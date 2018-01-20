package com.ujigu.secure.mq;

import javax.annotation.Resource;
import javax.jms.Message;

import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.mq.bean.MqMsg;
import com.ujigu.secure.mq.receiver.MqMsgListener;
import com.ujigu.secure.sms.dto.Sms;
import com.ujigu.secure.sms.service.SmsService;

public class SmsMsgListener extends MqMsgListener {

	@Resource
	private SmsService smsService;

	@Override
	protected void handleMsg(Message message, MqMsg msg) throws Throwable {
		if (!(msg instanceof Sms)) {
			LogUtils.warn("the msg is not a sms msg, message(%s) class(%s) ", JsonUtil.create().toJson(msg),
					msg.getClass().getName());
			return;
		}

		Sms sms = (Sms) msg;

		smsService.send(sms);

	}

}
