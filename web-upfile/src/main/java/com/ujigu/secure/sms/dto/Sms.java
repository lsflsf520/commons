package com.ujigu.secure.sms.dto;

import java.util.List;

import com.ujigu.secure.mq.bean.MqMsg;

public class Sms extends MqMsg{
	
	private String appId;
	private String module;
	private String phone;
	private String tmplId;
	private boolean isBatch;
	private List<String> params;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getTmplId() {
		return tmplId;
	}
	public void setTmplId(String tmplId) {
		this.tmplId = tmplId;
	}
	public boolean isBatch() {
		return isBatch;
	}
	public void setBatch(boolean isBatch) {
		this.isBatch = isBatch;
	}
	public List<String> getParams() {
		return params;
	}
	public void setParams(List<String> params) {
		this.params = params;
	}
	

}
