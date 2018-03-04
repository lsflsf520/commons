package com.xyz.tools.mq.receiver;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.ThreadUtil;
import com.xyz.tools.common.utils.LogUtils.IntfType;
import com.xyz.tools.mq.bean.MqMsg;

public abstract class MqMsgListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		if(message == null){
			LogUtils.warn("received blank message");
			return;
		}
		if(!(message instanceof TextMessage)){
			LogUtils.warn("only TextMessage can be handled, message(%s) class(%s) ", JsonUtil.create().toJson(message), message.getClass().getName());
			return;
		}
		TextMessage textMsg = (TextMessage)message;
		MqMsg msg = null;
		try {
			String text = textMsg.getText();
			if(StringUtils.isBlank(text)){
				LogUtils.warn("content is null for message(%s)", JsonUtil.create().toJson(textMsg));
				return;
			}
			String msgClazz = getMsgClazz(text);
			Class<?> clazz = Class.forName(msgClazz);
			msg = (MqMsg)new Gson().fromJson(text, clazz);
			ThreadUtil.getTraceMsgId(msg.getMsgId());
			LogUtils.logIntf(IntfType.IN, this.getClass().getSimpleName(), text);
			
			handleMsg(message, msg);
			
			//message.acknowledge();
		} catch (Throwable th) {
			LogUtils.error("handle msg(%s) error", th, textMsg);
		}finally {
			ThreadUtil.clear();
        }
	}
	
	private String getMsgClazz(String text){
		
		String[] parts = text.split("\"msgClazzName\":\"");
		if(parts.length != 2){
			throw new BaseRuntimeException("ILLEGAL_MSG", "cannot find msg class from text(" + text + ")");
		}
		
		int quoteIndex = parts[1].lastIndexOf("\"");
		if(quoteIndex < 0){
			throw new BaseRuntimeException("ILLEGAL_MSG", "cannot find msg class from text(" + text + ")");
		}
		
		return parts[1].substring(0, quoteIndex);
	}
	
	abstract protected void handleMsg(Message message, MqMsg msg) throws Throwable;

}
