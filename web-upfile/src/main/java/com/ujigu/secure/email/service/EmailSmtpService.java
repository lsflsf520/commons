package com.ujigu.secure.email.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.jms.Destination;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sun.mail.smtp.SMTPTransport;
import com.ujigu.csai.msglog.constant.MsgStatus;
import com.ujigu.csai.msglog.entity.MsgSendLog;
import com.ujigu.csai.msglog.service.MsgSendLogService;
import com.ujigu.secure.common.bean.MsgType;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.email.dto.Email;
import com.ujigu.secure.email.service.EmailConfig.SenderInfo;
import com.ujigu.secure.email.utils.MsgUtil;
import com.ujigu.secure.mq.sender.ActiveMQMsgSender;

/**
 * 
 * @author lsf
 *
 */
@Service
public class EmailSmtpService {
	
	@Resource
	private MsgSendLogService msgSendLogService;
	
	@Resource(name = "emailQueueMqMsgSender")
	private ActiveMQMsgSender emailQueueMqMsgSender;
	
	@Resource(name = "emailQueueDestination")
	private Destination emailQueueDestination;
	
	private String getMessage(String reply) {
		String[] arr = reply.split("#");

		String messageId = null;

		if (arr[0].equalsIgnoreCase("250 ")) {
			messageId = arr[1];
		}

		return messageId;
	}
	
	public ResultModel asyncSend(Email email){
		final SenderInfo senderinfo = EmailConfig.getSenderInfo(email.getApiuser());
		checkEmail(email, senderinfo, email.getApiuser());
		
		email.init();
		emailQueueMqMsgSender.sendMsg(emailQueueDestination, email);
		
		return new ResultModel(true);
	}
	
	protected void checkEmail(Email email, SenderInfo senderinfo, String apiuser){
		if(senderinfo == null){
			throw new BaseRuntimeException("ILLEGAL_APIUSER", "配置不存在！", "not exist config for apiuser " + apiuser);
		}
		if(!EmailConfig.checkEmail(email.getTos()).isSuccess()){
			throw new BaseRuntimeException("ILLEGAL_ADDR", "目标邮件地址格式错误！", "illegal addr " + email.getTos());
		}
		if(senderinfo.isEventType() && email.getTos().size() > 1){
			throw new BaseRuntimeException("ILLEGAL_STATE", "此apiuser不能批量发送邮件", "apiuser " + apiuser);
		}
		
		if(!EmailConfig.isWhiteModule(email.getModule())){
			throw new BaseRuntimeException("ILLEGAL_MODULE", "非授信的邮件模块", "module("+email.getModule()+"),email("+email.getTos()+") not exist white list");
		}

        List<String> checkedTos = new ArrayList<>();
		boolean isBatch = email.getTos().size() > 1;
		for(String to : email.getTos()){
			try{
				MsgUtil.checkValve(to, MsgType.EMAIL, isBatch);
				checkedTos.add(to);
			} catch (BaseRuntimeException e){
				LogUtils.warn(e.getMessage());
			}
		}
		
		if(CollectionUtils.isEmpty(checkedTos)){
			throw new BaseRuntimeException("NO_CHECKED_PASS_EMAIL", "没有检测通过的邮箱地址");
		}
		
		email.setTos(checkedTos);
	}
	
	
	public ResultModel send(Email email)  throws MessagingException, UnsupportedEncodingException{
		final SenderInfo senderinfo = EmailConfig.getSenderInfo(email.getApiuser());
		checkEmail(email, senderinfo, email.getApiuser());
		if(StringUtils.isBlank(email.getTitle()) || StringUtils.isBlank(email.getContent())){
			return new ResultModel("ILLEGAL_PARAM", "标题或者内容不能为空！");
		}
		
		Session mailSession = Session.getInstance(EmailConfig.getMailProps(), new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(senderinfo.getApiuser(), senderinfo.getApikey());
			}
		});

		MimeMessage message = new MimeMessage(mailSession);
		// 发信人
		message.setFrom(new InternetAddress(senderinfo.getFromdomain(), senderinfo.getFromname(), "UTF-8"));
		// 收件人地址
		message.addRecipient(RecipientType.TO, new InternetAddress(email.getTos().get(0)));
		// 邮件主题
		message.setSubject(email.getTitle(), "UTF-8");
		// 邮件回复地址
		if(StringUtils.isNotBlank(email.getReplyTo())){
			message.setReplyTo(new Address[] { new InternetAddress(email.getReplyTo()) });
		} else if(StringUtils.isNotBlank(senderinfo.getReplyTo())){
			message.setReplyTo(new Address[] { new InternetAddress(senderinfo.getReplyTo()) });
		}
		
		Xsmtpapi xsmtpapi = null;
		if(email.getTos().size() > 1){
			xsmtpapi = new Xsmtpapi(email.getTos());
		}
		
		if(!CollectionUtils.isEmpty(email.getSubMap())){
			if(xsmtpapi == null){
				xsmtpapi = new Xsmtpapi(email.getTos(), email.getSubMap());
			} else {
				xsmtpapi.setSubMap(email.getSubMap());
			}
			if(!CollectionUtils.isEmpty(email.getSectionMap())){
				xsmtpapi.setSectionMap(email.getSectionMap());
			}
		}
		message.setHeader("X-SMTPAPI", new String(Base64.encodeBase64(xsmtpapi.toString().getBytes())));

		Multipart multipart = new MimeMultipart("alternative");

		// 添加html形式的邮件正文
		String html = "<html><head></head><body>" + email.getContent() + "</body></html> ";
		BodyPart contentPart = new MimeBodyPart();
		contentPart.setHeader("Content-Type", "text/html;charset=UTF-8");
		contentPart.setHeader("Content-Transfer-Encoding", "base64");
		contentPart.setContent(html, "text/html;charset=UTF-8");
		multipart.addBodyPart(contentPart);

		// 添加附件 ( smtp 方式没法使用文件流 )
		if(!CollectionUtils.isEmpty(email.getAttachments())){
			for(String attach : email.getAttachments()){
				File file = new File(EmailConfig.getAttachBaseDir() + attach);
				
				if(file.exists()){
					BodyPart attachmentBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(file);
					attachmentBodyPart.setDataHandler(new DataHandler(source));
					attachmentBodyPart.setFileName(MimeUtility.encodeWord(file.getName()));
					multipart.addBodyPart(attachmentBodyPart);
				}
			}
		}
		
		message.setContent(multipart);

		String errorMsg = null;
		SMTPTransport transport = (SMTPTransport) mailSession.getTransport("smtp");
		try{
			// 连接sendcloud服务器，发送邮件
			transport.connect();
			transport.sendMessage(message, message.getRecipients(RecipientType.TO));
			
			String messageId = getMessage(transport.getLastServerResponse());
			
			boolean isBatch = email.getTos().size() > 1;
			List<String> msgIds = new ArrayList<>();
			for(String to : email.getTos()){
				msgIds.add(messageId + "#" + to);
				MsgUtil.incrSendNum(email.getTos().get(0), isBatch);
			}
			
			logEmail(email, messageId, MsgStatus.SUCCESS, null);
			
			return new ResultModel(msgIds);
		} catch(MessagingException me){
			if(me.getCause() instanceof SocketTimeoutException){
				LogUtils.error("send email error, apiuser:%s,to:%s,title:%s", me.getCause(), email.getApiuser(), email.getTos(), email.getTitle());
				//TODO 记录处理发送失败的邮件信息
				logEmail(email, null, MsgStatus.CONFIRM, me.getCause().getMessage());
				return new ResultModel("READ_RESULT_TIMEOUT", "读取邮件发送结果超时，请确认邮件是否已妥投！");
			} 
			errorMsg = me.getMessage();
			LogUtils.error("send email error, apiuser:%s,to:%s,title:%s", me, email.getApiuser(), email.getTos(), email.getTitle());
		} catch (Exception e){
			errorMsg = e.getMessage();
			LogUtils.error("send email error, apiuser:%s,to:%s,title:%s", e, email.getApiuser(), email.getTos(), email.getTitle());
		} finally {
			if(transport != null){
				transport.close();
			}
		}
		
		logEmail(email, null, MsgStatus.FAIL, errorMsg);
		
		return new ResultModel("SEND_ERR", "邮件发送出现异常！");
	}
	
	protected void logEmail(Email email, String messageId, MsgStatus status, String errorMsg) {
		List<MsgSendLog> msgLogs = new ArrayList<>();
		if(StringUtils.isBlank(messageId)){
			if(StringUtils.isBlank(email.getMsgId())){
				messageId = ThreadUtil.getTraceMsgId();
			}else{
				messageId = email.getMsgId();
			}
		}
		for(String to : email.getTos()){
			MsgSendLog msglog = new MsgSendLog();
			msglog.setModule(email.getModule());
			msglog.setSender(email.getApiuser());
			msglog.setCreateTime(new Date());
			msglog.setMsgId(messageId);
			msglog.setSrcIp(email.getSrcIP());
			msglog.setStatus(status);
			msglog.setType(MsgType.EMAIL);
			if(StringUtils.isNotBlank(email.getTmplId())){
				msglog.setTmplId(email.getTmplId());
			} else if(StringUtils.isNotBlank(email.getTitle())){
				msglog.setTmplId(email.getTitle().substring(0, email.getTitle().length() > 32 ? 32 : email.getTitle().length()));
			}
			msglog.setToAddr(to);
			
			Map<String, Object> extras = new HashMap<>();
			if(!CollectionUtils.isEmpty(email.getSubMap())){
				extras.put("subMap", email.getSubMap());
			}
			if(!CollectionUtils.isEmpty(email.getSectionMap())){
				extras.put("sectionMap", email.getSectionMap());
			}
			if(StringUtils.isNotBlank(email.getContent())){
				extras.put("content", email.getContent());
			}
			if(StringUtils.isNotBlank(email.getTitle())){
				extras.put("title", email.getTitle());
			}
			if(!CollectionUtils.isEmpty(email.getAttachments())){
				extras.put("attachments", email.getAttachments());
			}
			if(StringUtils.isNotBlank(errorMsg)){
				extras.put("errorMsg", errorMsg);
			}
			
			String extraInfo = JsonUtil.create().toJson(extras);
			if(extraInfo.length() > 100){
				Date currDate = new Date();
				String generationfileName = DateUtil.formatDate(currDate,
						"yyyyMMddHHmmss")
						+ new Random(System.currentTimeMillis()).nextInt(1000);
				String filepath = EmailConfig.getAttachBaseDir() + "msglog/" + (StringUtils.isNotBlank(email.getModule()) ? email.getModule() : "extra")
						+ File.separator + DateUtil.getMonthStr(currDate) ;
				File dir = new File(filepath);
				if(!dir.exists()){
					dir.mkdirs();
				}
				
				filepath += File.separator + generationfileName + ".txt";
				try {
					FileUtils.write(new File(filepath), extraInfo, "UTF-8");
				} catch (IOException e) {
					LogUtils.warn("error write file %s with content %s", filepath, extraInfo);
				}
				
				msglog.setExtraInfo(filepath);
			}else {
				msglog.setExtraInfo(extraInfo);
			}
			
			msgLogs.add(msglog);
		}
		msgSendLogService.insertBatch(msgLogs);
	}
	
	/*public static void main(String[] args) throws UnsupportedEncodingException, MessagingException {

		List<String> toList = new ArrayList<String>();
		toList.add("lsflsf520@126.com");
		toList.add("546076948@qq.com");

		Map<String, List<String>> subMap = new HashMap<String, List<String>>();
		subMap.put("%email%", toList);
		subMap.put("%name%", new ArrayList<String>(Arrays.asList("name1 %num1%", "name2 %num2%")));

		Map<String, String> sectionMap = new HashMap<String, String>();
		sectionMap.put("%num1%", "1111111111111111111");
		sectionMap.put("%num2%", "2222222222222222222");
		
		Email email = new Email();
		email.setApiuser("postmaster@passport-shangxueba.sendcloud.org");
		email.setTos(Arrays.asList("lsflsf520@126.com", "546076948@qq.com"));
		email.setTitle("Smtp邮件测试");
		email.setContent("<p>欢迎使用<a href='http://sendcloud.sohu.com'>SendCloud!</a> %email%  %name%</p>");
//		email.setSubMap(subMap);
//		email.setSectionMap(sectionMap);

		ResultModel result = new EmailSmtpService().send(email);
		
		System.out.println(JsonUtil.create().toJson(result));
	}*/
	
}

class Xsmtpapi {

	JSONObject x = new JSONObject();

	public Xsmtpapi(List<String> toList) {
		x.put("to", toList);
	}
	
	public void setSubMap(Map<String, List<String>> subMap){
		x.put("sub", subMap);
	}
	
	public void setSectionMap(Map<String, String> sectionMap){
		x.put("section", sectionMap);
	}

	public Xsmtpapi(List<String> toList, Map<String, List<String>> subMap) {
		x.put("to", toList);
		x.put("sub", subMap);
	}

	@Override
	public String toString() {
		return x.toString();
	}
}
