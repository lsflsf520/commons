package com.ujigu.secure.sms.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.jms.Destination;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.cloopen.rest.sdk.CCPRestSDK;
import com.ujigu.csai.msglog.constant.MsgStatus;
import com.ujigu.csai.msglog.entity.MsgSendLog;
import com.ujigu.csai.msglog.service.MsgSendLogService;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.bean.MsgType;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.LogUtils.IntfType;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.email.service.EmailConfig;
import com.ujigu.secure.email.utils.MsgUtil;
import com.ujigu.secure.mq.sender.ActiveMQMsgSender;
import com.ujigu.secure.sms.dto.Sms;

@Service
public class SmsService {
	
	@Resource
	private MsgSendLogService msgSendLogService; 
	
	@Resource(name = "smsQueueMqMsgSender")
	private ActiveMQMsgSender smsQueueMqMsgSender;
	
	@Resource(name = "smsQueueDestination")
	private Destination smsQueueDestination;
	
	public ResultModel asyncSend(Sms... smsArr){
		List<Sms> smsList = commonCheck(smsArr);
		
		for(Sms sms : smsList){
			sms.init();
			smsQueueMqMsgSender.sendMsg(smsQueueDestination, sms);
		}
		
		return new ResultModel(true);
	}
	
	private List<Sms> commonCheck(Sms... smsArr) {
		List<Sms> checkedSmsList = new ArrayList<>();
		boolean isBatch = smsArr.length > 1;
		for(Sms sms : smsArr){
			if(!RegexUtil.isPhone(sms.getPhone())){
				throw new BaseRuntimeException("ILLEGAL_PHONE", "手机号("+sms.getPhone()+")格式不正确");
			}
			if(!isWhiteModule(sms.getModule())){
				throw new BaseRuntimeException("ILLEGAL_MODULE", "非授信的模块", "模块名("+sms.getModule()+"),手机号("+sms.getPhone()+")不在白名单中");
			}
			if(StringUtils.isBlank(sms.getAppId())){
				throw new BaseRuntimeException("ILLEGAL_APPID", "参数错误");
			}
			if(StringUtils.isBlank(sms.getTmplId())){
				throw new BaseRuntimeException("ILLEGAL_TMPLID", "参数错误");
			}
			
			try{
				MsgUtil.checkValve(sms.getPhone(), MsgType.SMS, isBatch);
				
				sms.setBatch(isBatch);
				checkedSmsList.add(sms);
			} catch (BaseRuntimeException e){
				LogUtils.warn(e.getMessage());
			}
		}
		
		if(CollectionUtils.isEmpty(checkedSmsList)){
			throw new BaseRuntimeException("NO_CHECKED_PASS_EMAIL", "没有检测通过的邮箱地址");
		}
		
		return checkedSmsList;
	}
	
	public ResultModel send(Sms sms){
		commonCheck(sms);
		
		String SMS_HOST = BaseConfig.getValue("cloopen.server.host", "app.cloopen.com");
		String SMS_PORT = BaseConfig.getValue("cloopen.server.port", "8883");
		String SMS_ACCSID = BaseConfig.getValue("cloopen.server.accountsid");
		String SMS_TOKEN = BaseConfig.getValue("cloopen.server.token");
		
		if(StringUtils.isBlank(SMS_ACCSID) || StringUtils.isBlank(SMS_TOKEN)){
			throw new BaseRuntimeException("LESS_CONFIG", "缺少配置", "cloopen.server.accountsid and cloopen.server.token should be config in application.properties");
		}
		if(!RegexUtil.isPhone(sms.getPhone())){
			throw new BaseRuntimeException("ILLEGAL_PHONE", "手机号("+sms.getPhone()+")格式不正确");
		}

		CCPRestSDK restAPI = new CCPRestSDK();
		restAPI.init(SMS_HOST, SMS_PORT);// 初始化服务器地址和端口，格式如下，服务器地址不需要写https://
		restAPI.setAccount(SMS_ACCSID, SMS_TOKEN);// 初始化主帐号和主帐号TOKEN
		restAPI.setAppId(sms.getAppId());// 初始化应用ID
		HashMap<String, Object> result = restAPI.sendTemplateSMS(sms.getPhone(), sms.getTmplId(), sms.getParams() == null ? new String[0] : sms.getParams().toArray(new String[0])); //"164388"  new String[]{"尚方宝剑", "2"}

		LogUtils.logIntf(IntfType.IN, "sms/send", result);
		
		if("000000".equals(result.get("statusCode"))){
			logSms(sms, null, MsgStatus.SUCCESS, null);
			MsgUtil.incrSendNum(sms.getPhone(), sms.isBatch());
			return new ResultModel(true);
		}
		
		String code = result.get("statusCode") == null ? GlobalResultCode.UNKNOWN_ERROR.getCode() : result.get("statusCode").toString();
		String errorMsg = result.get("statusMsg") == null ? GlobalResultCode.UNKNOWN_ERROR.getFriendlyMsg() : result.get("statusMsg").toString();
		ResultModel resultModel = new ResultModel(code, errorMsg);
		
		logSms(sms, null, MsgStatus.FAIL, "code:" + code + ",errorMsg:" + errorMsg);
		
		return resultModel;
	}
	
	private void logSms(Sms sms, String messageId, MsgStatus status, String errorMsg){
		if(StringUtils.isBlank(messageId)){
			if(StringUtils.isBlank(sms.getMsgId())){
				messageId = ThreadUtil.getTraceMsgId();
			}else{
				messageId = sms.getMsgId();
			}
		}
		
		MsgSendLog msglog = new MsgSendLog();
		msglog.setModule(sms.getModule());
		msglog.setSender(sms.getAppId());
		msglog.setCreateTime(new Date());
		msglog.setMsgId(messageId);
		msglog.setSrcIp(sms.getSrcIP());
		msglog.setStatus(status);
		msglog.setType(MsgType.SMS);
		msglog.setTmplId(sms.getTmplId());
		msglog.setToAddr(sms.getPhone());
		
		Map<String, Object> extras = new HashMap<>();
		if(!CollectionUtils.isEmpty(sms.getParams())){
			extras.put("params", sms.getParams());
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
			String filepath = EmailConfig.getAttachBaseDir() + "msglog/" + (StringUtils.isNotBlank(sms.getModule()) ? sms.getModule() : "extra")
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
		
		msgSendLogService.insert(msglog);
	}
	
	private static boolean isWhiteModule(String module){
		String[] whiteModules = BaseConfig.getValueArr("sms.white.module");
		
		return StringUtils.isNotBlank(module) && whiteModules != null && whiteModules.length > 0 && Arrays.asList(whiteModules).contains(module);
	}

}
