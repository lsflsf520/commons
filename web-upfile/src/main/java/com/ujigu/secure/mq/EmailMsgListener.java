package com.ujigu.secure.mq;

import javax.annotation.Resource;

import javax.jms.Message;
import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.email.dto.Email;
import com.ujigu.secure.email.service.EmailHttpService;
import com.ujigu.secure.email.service.EmailSmtpService;
import com.ujigu.secure.mq.bean.MqMsg;
import com.ujigu.secure.mq.receiver.MqMsgListener;

public class EmailMsgListener extends MqMsgListener{
	
	@Resource
	private EmailHttpService emailHttpService;
	
	@Resource
	private EmailSmtpService emailSmtpService;
	
	@Override
	protected void handleMsg(Message message, MqMsg msg) throws Throwable{
		if(!(msg instanceof Email)){
			LogUtils.warn("the msg is not a email msg, message(%s) class(%s) ", JsonUtil.create().toJson(msg), msg.getClass().getName());
			return;
		}
		
		Email email = (Email)msg;
		if(StringUtils.isNotBlank(email.getTmplId())){
			emailHttpService.send(email);
		} else {
			emailSmtpService.send(email);
		}
		
	}

}
