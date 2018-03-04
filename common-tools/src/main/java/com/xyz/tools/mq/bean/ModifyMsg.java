package com.xyz.tools.mq.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.ibatis.mapping.SqlCommandType;

import com.xyz.tools.common.utils.DataConvertUtil;

public class ModifyMsg extends MqMsg{
	
	private SqlCommandType cmdType;
	private boolean ignoreTopicMsg;
	private String tableName;
	private Long operUid;
	private String operUserName;
	private Map<String, Object> data;
	
	public SqlCommandType getCmdType() {
		return cmdType;
	}
	public void setCmdType(SqlCommandType cmdType) {
		this.cmdType = cmdType;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public Long getOperUid() {
		return operUid;
	}
	public void setOperUid(Long operUid) {
		this.operUid = operUid;
	}
	public String getOperUserName() {
		return operUserName;
	}
	public void setOperUserName(String operUserName) {
		this.operUserName = operUserName;
	}
	public Map<String, Object> getData() {
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public void setData(Object data) {
		if(data != null){
			if(data instanceof Map){
				this.data = (Map<String, Object>)data;
			} else if(data instanceof Number  || data instanceof String || data instanceof Boolean || data instanceof Character){
				this.data = new HashMap<>();
				this.data.put("model", data);
			}else if(data instanceof Collection){
				this.data = new HashMap<>();
				//TODO 这个地方还需要再测试一下
				this.data.put("model", data);
			}else {
				this.data = new HashMap<>();
				BeanMap beanMap = new BeanMap(data);
				for(Object key : beanMap.keySet()){
					if(key != null && !"class".equals(key) && !"pageInfo".equals(key) && !"ordseg".equals(key) && !"queryParam".equals(key)){
						this.data.put(key.toString(), beanMap.get(key));
					}
				}
			}
		}
	}
	public boolean isIgnoreTopicMsg() {
		return ignoreTopicMsg;
	}
	public void setIgnoreTopicMsg(boolean ignoreTopicMsg) {
		this.ignoreTopicMsg = ignoreTopicMsg;
	}
	
	public int parseInt(String key){
		Object obj = this.data.get(key);
		
		return DataConvertUtil.parseInt(obj);
	}
	
	public String parseStr(String key){
		Object obj = this.data.get(key);
		
		return DataConvertUtil.parseStr(obj);
	}
	
	public boolean parseBool(String key){
		Object obj = this.data.get(key);
		
		return DataConvertUtil.parseBool(obj);
	}
	
}
