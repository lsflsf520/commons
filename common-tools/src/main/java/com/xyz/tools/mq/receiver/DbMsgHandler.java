package com.xyz.tools.mq.receiver;

import javax.jms.Message;

import com.xyz.tools.mq.bean.ModifyMsg;

/**
 * 
 * @author lsf
 *
 */
public interface DbMsgHandler {

	/**
	 * 处理activeMQ发送过来的消息
	 * @param cmdType
	 * @param tableName
	 * @param data
	 */
    void handleMsg(Message message, ModifyMsg modifyMsg);
    
    /**
     * 
     * @return 返回支持处理的表名，多个表名以英文逗号分隔
     */
    String supportTables();
}
