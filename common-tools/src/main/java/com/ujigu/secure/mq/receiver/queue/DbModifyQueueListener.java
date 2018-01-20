package com.ujigu.secure.mq.receiver.queue;

import javax.jms.Destination;
import javax.jms.Message;

import com.ujigu.secure.mq.bean.ModifyMsg;
import com.ujigu.secure.mq.receiver.topic.DbModifyTopicListener;
import com.ujigu.secure.mq.sender.ActiveMQMsgSender;

/**
 * 
 * @author lsf
 *
 */
public class DbModifyQueueListener extends DbModifyTopicListener{
	
	private ActiveMQMsgSender activeMQMsgSender;
	private Destination destination;

	public void setActiveMQMsgSender(ActiveMQMsgSender activeMQMsgSender) {
		this.activeMQMsgSender = activeMQMsgSender;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}
  
	@Override
	protected void postHandle(Message message, ModifyMsg modifyMsg) {
		if(!modifyMsg.isIgnoreTopicMsg() && activeMQMsgSender != null && destination != null){
			activeMQMsgSender.sendMsg(destination, modifyMsg);
		}
	}
	
	protected Class<?> getSuperHandlerClazz(){
		return DbMsgQueueHandler.class;
	}
}
