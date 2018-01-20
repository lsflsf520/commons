package com.ujigu.acl.datavalid.bean;

import java.util.HashMap;
import java.util.Map;

import com.ujigu.secure.common.exception.BaseRuntimeException;

public class PrivContext {
	
	private Map<String, Object> context = new HashMap<>();
	
	public PrivContext() {
	}
	
	public PrivContext(int workerId) {
		putIfAbsentWorkerId(workerId);
//		putIfAbsentWebappId(webappId);
	}
	
	public PrivContext put(String key, Object value){
		if(("_workerId".equals(key) && existKey("_workerId"))){
			throw new BaseRuntimeException("ALREADY_EXIST", "不支持的操作", "_workerId cannot be modified");
		}
		
		context.put(key, value);
		
		return this;
	}
	
	public PrivContext putIfAbsent(String key, Object value){
		if(!existKey(key)){
			put(key, value);
		}
		
		return this;
	}
	
	@SuppressWarnings("all")
	public <T> T getValue(String key){
		
		return (T) context.get(key);
	}
	
	public boolean existKey(String key){
		
		return context.containsKey(key);
	}
	
	private void putIfAbsentWorkerId(int _workerId){
		if(!existKey("_workerId")){
			context.put("_workerId", _workerId);
		}
	}
	
    public Integer getWorkerId(){
		
		return (Integer)getValue("_workerId");
	}
	
	public PrivContext putIfAbsentDepartId(int departId){
		if(!existKey("departId")){
			context.put("departId", departId);
		}
		
		return this;
	}
	
    public Integer getDepartId(){
    	
    	return (Integer)getValue("departId");
	}
	
	public PrivContext putIfAbsentWebappId(int webappId){
		if(!existKey("webappId")){
			context.put("webappId", webappId);
		}
		
		return this;
	}
	
	/*public Integer getWebappId(){
		
		return (Integer)getValue("webappId");
	}*/
	

}
