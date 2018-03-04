package com.xyz.tools.mq.receiver.topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.mq.receiver.DbModifyMsgListener;
import com.xyz.tools.mq.receiver.DbMsgHandler;

/**
 * 
 * @author lsf
 *
 */
public class DbModifyTopicListener extends DbModifyMsgListener implements ApplicationContextAware{
	
	private Map<String/*tableName*/, List<DbMsgHandler>> tableHandlerMap = new HashMap<>();

	public void setTblHandlerMap(Map<String, DbMsgHandler> tableHandlerMap) {
		if(!CollectionUtils.isEmpty(tableHandlerMap)){
			for(Entry<String, DbMsgHandler> entry : tableHandlerMap.entrySet()){
				if(StringUtils.isBlank(entry.getKey()) || entry.getValue() == null){
					LogUtils.warn("table name %s or DbMsgHandler %s is blank", entry.getKey(), entry.getValue());
					continue;
				}
				String[] tableArr = entry.getKey().split(",");
				for(String tableName : tableArr){
					if(StringUtils.isBlank(tableName)){
						continue;
					}
					addMsgHandler(tableName, entry.getValue());
				}
			}
		}
		
	}
	
	private void addMsgHandler(String tableName, DbMsgHandler msgHandler){
		tableName = tableName.trim().toLowerCase();
		List<DbMsgHandler> handlers = this.tableHandlerMap.get(tableName);
		if(handlers == null){
			handlers = new ArrayList<>();
			this.tableHandlerMap.put(tableName, handlers);
		}
		
		if(handlers.isEmpty()){
			handlers.add(msgHandler);
		} else {
			//如果该tableName的处理器列表中已经包含了 msgHandler 类型的处理器，则直接忽略该处理器
			for(DbMsgHandler existHandler : handlers){
				if(existHandler.getClass().getName().equals(msgHandler.getClass().getName())){
					LogUtils.warn("db msg handler %s has exist for tableName %s", msgHandler.getClass().getName(), tableName);
					return;
				}
			}
			
			handlers.add(msgHandler);
		}
	}

	@Override
	protected List<DbMsgHandler> getDbMsgHandlers(String tableName) {
		
		return  tableHandlerMap.get(tableName);
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Map<String, ?> handlerMap = context.getBeansOfType(getSuperHandlerClazz());
		if(!CollectionUtils.isEmpty(handlerMap)){
			for(String handlerName : handlerMap.keySet()){
				DbMsgHandler msgHandler = (DbMsgHandler)handlerMap.get(handlerName);
				
				String supportTableStr = msgHandler.supportTables();
				if(StringUtils.isBlank(supportTableStr)){
					LogUtils.warn("no support tables defined for class %s", msgHandler.getClass().getName());
					continue;
				}
				
				String[] tables = supportTableStr.split(",");
				for(String table : tables){
					addMsgHandler(table, msgHandler);
				}
			}
		}
	}

	protected Class<?> getSuperHandlerClazz(){
		return DbMsgTopicHandler.class;
	}
  
}
