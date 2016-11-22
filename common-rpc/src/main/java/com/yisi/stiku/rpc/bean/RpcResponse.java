package com.yisi.stiku.rpc.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author shangfeng
 *
 */
public final class RpcResponse implements Serializable {
	
	private static final long serialVersionUID = 5887232731148682128L;
	
	private final String messageId;
	private final Object returnValue;
	private final Throwable exception;
	
	private final Map<String, Object> extraInfoMap = new HashMap<String, Object>();
	
	/**
	 * 构造心跳响应
	 */
	public RpcResponse(){
		this.messageId = "PONG";
		this.returnValue = null;
		this.exception = null;
	}
	
	public RpcResponse(final String messageId, final Object returnValue) {
		this.messageId = messageId;
		this.returnValue = returnValue;
		this.exception = null;
	}
	
	public RpcResponse(final String messageId, final Throwable exception) {
		this.messageId = messageId;
		this.returnValue = null;
		this.exception = exception;
	}

	public String getMessageId() {
		return messageId;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public Throwable getException() {
		return exception;
	}
	
	public void addExtraInfo(String key, Object val){
		this.extraInfoMap.put(key, val);
	}
	
	@SuppressWarnings("all")
	public <T> T getExtraInfo(String key){
		return (T)this.extraInfoMap.get(key);
	}
	
	public void putAll(Map<String, Object> extraInfoMap){
		this.extraInfoMap.putAll(extraInfoMap);
	}
	
	public boolean isError(){
		return this.getException() != null;
	}
	
}
