package com.ujigu.secure.email.service;


import java.io.File;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sendcloud.sdk.builder.SendCloudBuilder;
import com.sendcloud.sdk.core.SendCloud;
import com.sendcloud.sdk.model.MailBody;
import com.sendcloud.sdk.model.SendCloudMail;
import com.sendcloud.sdk.model.TemplateContent;
import com.sendcloud.sdk.util.ResponseData;
import com.ujigu.csai.msglog.constant.MsgStatus;
import com.ujigu.csai.msglog.service.MsgSendLogService;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.email.dto.Email;
import com.ujigu.secure.email.service.EmailConfig.SenderInfo;
import com.ujigu.secure.email.utils.MsgUtil;


/**
 * 
 * @author lsf
 *
 */
@Service
public class EmailHttpService extends EmailSmtpService {
	
	@Resource
	private MsgSendLogService msgSendLogService;
	
	public ResultModel send(Email email) {
		final SenderInfo senderinfo = EmailConfig.getSenderInfo(email.getApiuser());
		checkEmail(email, senderinfo, email.getApiuser());
		if(StringUtils.isBlank(email.getTmplId())){
			return new ResultModel("ILLEGAL_TMPLID", "参数错误");
		}
		
		MailBody body = new MailBody();
		// 设置 From
		body.setFrom(senderinfo.getFromdomain());
		// 设置 FromName
		body.setFromName(senderinfo.getFromname());
		// 设置 ReplyTo
		if(StringUtils.isNotBlank(email.getReplyTo())){
			body.setReplyTo(email.getReplyTo());
		}else if(StringUtils.isNotBlank(senderinfo.getReplyTo())){
			body.setReplyTo(senderinfo.getReplyTo());
		}
		// 设置标题
		body.setSubject(email.getTitle());
		
		if(!CollectionUtils.isEmpty(email.getAttachments())){
			for(String attach : email.getAttachments()){
				File file = new File(EmailConfig.getAttachBaseDir() + attach);
				if(file.exists()){
					// 添加文件附件
					body.addAttachments(file);
				}
			}
		}
		
		// 此时, receiver 中添加的 to, cc, bcc 均会失效
		body.addXsmtpapi("to", email.getTos());
		if(!CollectionUtils.isEmpty(email.getSubMap())){
			body.addXsmtpapi("sub", email.getSubMap());
			if(!CollectionUtils.isEmpty(email.getSectionMap())){
				body.addXsmtpapi("section", email.getSectionMap());
			}
		}

		// 使用邮件模板
		TemplateContent content = new TemplateContent();
		content.setTemplateInvokeName(email.getTmplId());
		
		/*TextContent content = new TextContent();
		content.setContent_type(ScContentType.html);
		content.setText("<html><p>亲爱的 %name%: </p> 您本月的支出为: %money% 元.</p></html>");*/

		SendCloudMail mail = new SendCloudMail();
		// 模板发送时, 必须使用 Xsmtpapi 来指明收件人; mail.setTo();
		mail.setBody(body);
		mail.setContent(content);

		SendCloud sc = SendCloudBuilder.build();
		ResponseData res = null;
		String errorMsg = null;
		try {
			res = sc.sendMail(mail, senderinfo.getApiuser(), senderinfo.getApikey());
			if(res != null && res.getResult() && (res.getStatusCode() == 200 || res.getStatusCode() == 40903)){
				logEmail(email, null, MsgStatus.SUCCESS, null);
				
				boolean isBatch = email.getTos().size() > 1;
				for(String to : email.getTos()){
					MsgUtil.incrSendNum(to, isBatch);
				}
				
				return new ResultModel(true);
			}
			errorMsg = "result:" + (res == null ? false : res.getResult()) + ",code:" + (res == null ? -1 : res.getStatusCode()) + ",errorMsg:" + (res == null ? "unknown" : res.getMessage());
		} catch (Throwable e) {
			errorMsg = e.getMessage();
			LogUtils.error("send email error, apiuser:%s,to:%s,title:%s", e, email.getApiuser(), email.getTos(), email.getTitle());
		}
		
		logEmail(email, null, MsgStatus.FAIL, errorMsg);
		
		return new ResultModel("" + (res == null ? -1 : res.getStatusCode()), (res == null ? errorMsg : res.getMessage()));
	}
	
	/*public static void main(String[] args) throws Throwable {
		Email email = new Email();
		email.setApiuser("postmaster@passport-shangxueba.sendcloud.org");
		email.setTmplId("csaimaibaoxian_tixing");
		email.setTitle("您有待支付的保险订单");
		email.setTos(Arrays.asList("lsflsf520@126.com"));
		
		Map<String, List<String>> subMap = new HashMap<String, List<String>>();
		subMap.put("%username%", Arrays.asList("尚方宝剑"));
		subMap.put("%orderdate%", Arrays.asList("2017-6-9 12:12:12"));
		subMap.put("%productname%", Arrays.asList("安心无忧 平安出行保险"));
		subMap.put("%paymoney%", Arrays.asList("18.8"));
		subMap.put("%expdate%", Arrays.asList("2017-7-9 12:12:12"));
		subMap.put("%premium%", Arrays.asList("18.8"));
		
		email.setSubMap(subMap);
		
		ResultModel result = new EmailHttpService().send(email);
		
		System.out.println(new Gson().toJson(result));
	}*/
	
}
