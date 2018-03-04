package com.xyz.tools.db.aop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.Destination;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RegexUtil;
import com.xyz.tools.common.utils.ThreadUtil;
import com.xyz.tools.db.utils.MybatisUtil;
import com.xyz.tools.mq.bean.ModifyMsg;
import com.xyz.tools.mq.sender.ActiveMQMsgSender;

@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class}) })
public class ModifyInterceptor implements Interceptor {
	
	private Map<String, Set<SqlCommandType>> msgTableCmdMap = new HashMap<>();
	
	private Set<String> ignoreTopicTables = new HashSet<>();
	
	private ActiveMQMsgSender activeMQMsgSender;
	
	private Destination destination;
	
	private List<DataModifyListener> dataModifyListeners;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		long startTime = System.currentTimeMillis();
		Object retVal = invocation.proceed();
		
//		final Executor executor = (Executor) invocation.getTarget();
		final Object[] queryArgs = invocation.getArgs();
		final MappedStatement ms = (MappedStatement)queryArgs[0];
		final Object parameter = queryArgs[1];
		
		try{
			String intfName = MybatisUtil.parseShortStmtName(ms.getId());
			LogUtils.logXN(intfName, startTime); //记录性能日志
			LogUtils.logOperLog(intfName, null, parameter);
			
			if(this.activeMQMsgSender != null){
				final SqlCommandType cmdType = ms.getSqlCommandType();
				BoundSql boundSql = ms.getBoundSql(parameter);
				String sql = boundSql.getSql().toLowerCase();
				String tableName = null;
				if(SqlCommandType.UPDATE.equals(cmdType)){
					List<String> tables = RegexUtil.extractGroups("\\s*update\\s+([^\\s]+)\\s+set\\s+\\.*", sql);
					if(!CollectionUtils.isEmpty(tables)){
						tableName = tables.get(0);
					}
				}
				
				if(StringUtils.isBlank(tableName) || SqlCommandType.INSERT.equals(cmdType)){
					List<String> tables = RegexUtil.extractGroups(".+into\\s+([^\\s]+)\\s*\\(.+", sql);
					if(!CollectionUtils.isEmpty(tables)){
						tableName = tables.get(0);
					}
				}
				
				if(StringUtils.isBlank(tableName) || SqlCommandType.DELETE.equals(cmdType)){
					List<String> tables = RegexUtil.extractGroups(".+from\\s+([^\\s]+)\\s*\\.*", sql);
					if(!CollectionUtils.isEmpty(tables)){
						tableName = tables.get(0);
					}
				}
				
				Set<SqlCommandType> cmdTypes = StringUtils.isBlank(tableName) ? new HashSet<SqlCommandType>() : msgTableCmdMap.get(tableName.trim());
				LogUtils.debug("tableName:%s, cmdType:%s, listening cmdTypes:%s", tableName, cmdType, cmdTypes);
				if(cmdTypes != null && cmdTypes.contains(cmdType)){
					ModifyMsg msg = new ModifyMsg();
					msg.init();
					msg.setCmdType(cmdType);
					msg.setTableName(tableName);
					msg.setOperUid(ThreadUtil.getUid() == null ? 0 : ThreadUtil.getUid());
					msg.setOperUserName(ThreadUtil.getRealName());
					msg.setData(parameter);
					if(this.ignoreTopicTables.contains(tableName)){
						msg.setIgnoreTopicMsg(true);
					}
						
					activeMQMsgSender.sendMsg(destination, msg);
					/*for(DataModifyListener listener : this.dataModifyListeners){
						listener.receivedModifyData(msg);
					}*/
				}
			}
		} catch (Throwable th){
			LogUtils.error("statementId:%s,data:%s", th, ms.getId(), new Gson().toJson(parameter));
		}
        
		return retVal;
	}
	
	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		String tablestr = properties.getProperty("msgTables");
		if(StringUtils.isNotBlank(tablestr)){
			String[] tables = tablestr.split(";");
			
			if(tables != null && tables.length >= 0){
				Set<SqlCommandType> cmdTypes = new HashSet<>(Arrays.asList(SqlCommandType.UPDATE, SqlCommandType.INSERT, SqlCommandType.DELETE));
				for(String table : tables){
					if(StringUtils.isBlank(table)){
						continue;
					}
					table = table.trim().toLowerCase();
					Set<SqlCommandType> currCmdTypes = cmdTypes;
					if(table.contains("(")){
						List<String> matchparts = RegexUtil.extractGroups("\\s*([^\\s\\(]+)\\s*\\(([\\w, ]+)*\\)", table);
						if(matchparts.size() != 2 || StringUtils.isBlank(matchparts.get(0)) || StringUtils.isBlank(matchparts.get(1))){
							throw new BaseRuntimeException("ILLEGAL_PARAM", "format for msgTables is not correct. table:" + table + ",matchparts:" + matchparts);
						}
						
						currCmdTypes = new HashSet<>();
						String[] cmdParts = matchparts.get(1).split(",");
						for(String cmdPart : cmdParts){
							if(StringUtils.isBlank(cmdPart)){
								continue;
							}
							cmdPart = cmdPart.trim().toUpperCase();
							SqlCommandType cmdType = SqlCommandType.valueOf(cmdPart);
							
							currCmdTypes.add(cmdType);
						}
						
						table = matchparts.get(0);
					}
					
					msgTableCmdMap.put(table, currCmdTypes);
					LogUtils.debug("listen table %s for command %s", table, currCmdTypes);
				}
			}
		}
		
		String ignoreTopicTableStr = properties.getProperty("ignoreTopicTables");
		if(StringUtils.isNotBlank(ignoreTopicTableStr)){
            String[] tables = ignoreTopicTableStr.split(";");
			
			if(tables != null && tables.length >= 0){
				for(String table : tables){
					this.ignoreTopicTables.add(table.trim().toLowerCase());
				}
			}
		}
	}
	
	public void setActiveMQMsgSender(ActiveMQMsgSender activeMQMsgSender) {
		this.activeMQMsgSender = activeMQMsgSender;
		if(this.dataModifyListeners == null){
			this.dataModifyListeners = new ArrayList<>();
		}
		
		this.dataModifyListeners.add(new MQModifyListener());
	}
	
	public void setDataModifyListeners(List<DataModifyListener> dataModifyListeners) {
		this.dataModifyListeners = dataModifyListeners;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	
	private class MQModifyListener implements DataModifyListener{

		@Override
		public void receivedModifyData(ModifyMsg modifyMsg) {
			activeMQMsgSender.sendMsg(destination, modifyMsg);
		}
		
		@Override
		public Set<String> interestTables() {
			return msgTableCmdMap.keySet();
		}

	}

	
}
