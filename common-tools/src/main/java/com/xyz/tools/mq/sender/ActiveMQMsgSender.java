package com.xyz.tools.mq.sender;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.LogUtils.IntfType;
import com.xyz.tools.mq.bean.MqMsg;

public class ActiveMQMsgSender {
	
	private JmsTemplate jmsTemplate;
	
	public ActiveMQMsgSender(JmsTemplate jmsTemplate){
		if(jmsTemplate == null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "jmsTemplate cannot be null for ActiveMQMsgSender init");
		}
		this.jmsTemplate = jmsTemplate;
	}
	
	public void sendMsg(String destinationName, final MqMsg data){
		if(data == null){
			LogUtils.warn("cannot send blank msg to destinationName %s", StringUtils.isBlank(destinationName) ? "default" : destinationName);
			return;
		}
		
		sendMsg(destinationName, JsonUtil.create().toJson(data));
	}
	
	public void sendMsg(Destination destination, final MqMsg data){
		if(data == null){
			LogUtils.warn("cannot send blank msg to destination %s", destination);
			return;
		}
		
		sendMsg(destination, JsonUtil.create().toJson(data));
	}
	
	private void sendMsg(String destinationName, final String text){
		if(StringUtils.isBlank(text)){
			LogUtils.warn("cannot send blank msg to destinationName %s", StringUtils.isBlank(destinationName) ? "default" : destinationName);
			return;
		}
		
        MessageCreator mc = new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		};
		
		if(StringUtils.isBlank(destinationName)){
			jmsTemplate.send(mc);
		}else{
			jmsTemplate.send(destinationName, mc);
		}
		
		LogUtils.logIntf(IntfType.OUT, "send_db_oper_msg", "destinationName=%s&data=%s", StringUtils.isBlank(destinationName) ? "default" : destinationName, text);
	}

	private void sendMsg(Destination destination, final String text){
		if(StringUtils.isBlank(text)){
			LogUtils.warn("cannot send blank msg to destination %s", destination == null ? "default" : JsonUtil.create().toJson(destination));
			return;
		}
		
		MessageCreator mc = new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		};
		
		if(destination == null){
			jmsTemplate.send(mc);
		}else{
			jmsTemplate.send(destination, mc);
		}
		
		LogUtils.logIntf(IntfType.OUT, "send_db_oper_msg", "destination=%s&data=%s", destination == null ? "default" : JsonUtil.create().toJson(destination), text);
	}
	
	/*public void sendMsg(String destinationName, final Map<String, Object> dataMap){
		if(CollectionUtils.isEmpty(dataMap)){
			LogUtils.warn("cannot send blank msg to destinationName %s", destinationName);
			return;
		}
		
		sendMsg(destinationName, new MessageCreator() {
	          public Message createMessage(Session session) throws JMSException {
	        	  MapMessage mapMessage = session.createMapMessage();
	        	  for(Entry<String, Object> entry : dataMap.entrySet()){
	        		  if(entry.getValue() instanceof String){
	        			  mapMessage.setString(entry.getKey(), (String)entry.getValue());
	        		  }
	        	  }
	        	  
	        	  return mapMessage;
	          }
	    });
	}
	
	public void sendMsg(Destination destination, final Map<String, Object> dataMap){
		if(CollectionUtils.isEmpty(dataMap)){
			LogUtils.warn("cannot send blank msg to destination %s", destination == null ? "default" : new Gson().toJson(destination));
			return;
		}
		
		sendMsg(destination, new MessageCreator() {
	          public Message createMessage(Session session) throws JMSException {
	        	  MapMessage mapMessage = session.createMapMessage();
	        	  for(Entry<String, Object> entry : dataMap.entrySet()){
	        		  if(entry.getValue() instanceof String){
	        			  mapMessage.setString(entry.getKey(), (String)entry.getValue());
	        		  }
	        	  }
	        	  
	        	  return mapMessage;
	          }
	    });
	}*/
	
}
