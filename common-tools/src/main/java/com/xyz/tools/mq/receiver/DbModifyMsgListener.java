package com.xyz.tools.mq.receiver;

import java.util.List;

import javax.jms.Message;

import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.mq.bean.ModifyMsg;
import com.xyz.tools.mq.bean.MqMsg;

public abstract class DbModifyMsgListener extends MqMsgListener{
	
	@Override
	protected void handleMsg(Message message, MqMsg msg) throws Throwable {
		if(!(msg instanceof ModifyMsg)){
			LogUtils.warn("only ModifyMsg can be handled, msg(%s) class(%s) ", JsonUtil.create().toJson(msg), message.getClass().getName());
			return ;
		}
		ModifyMsg modifyMsg = (ModifyMsg)msg;
		preHandle(message, modifyMsg);//消息前置处理
		List<DbMsgHandler> handlers = getDbMsgHandlers(modifyMsg.getTableName());
		if(!CollectionUtils.isEmpty(handlers)){
			for(DbMsgHandler handler : handlers){
				try{
					LogUtils.info("tableName:%s, handler:%s", modifyMsg.getTableName(), handler.getClass().getName());
					handler.handleMsg(message, modifyMsg);
				} catch(Exception e){
					LogUtils.error("handle db msg error,cmdType:%s,tableName:%s,handler:%s, data:%s", e, modifyMsg.getCmdType(), modifyMsg.getTableName(), handler.getClass().getName(), modifyMsg.getData() == null ? "" : new Gson().toJson(modifyMsg.getData()));
				}
			}
		}
		
		postHandle(message, modifyMsg); //消息后置处理
		
	}
	
	protected void preHandle(Message message, ModifyMsg modifyMsg){
	}
	
	protected void postHandle(Message message, ModifyMsg modifyMsg){
	}
	
	abstract protected List<DbMsgHandler> getDbMsgHandlers(String tableName);
}
