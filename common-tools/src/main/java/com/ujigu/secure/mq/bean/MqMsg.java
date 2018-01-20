package com.ujigu.secure.mq.bean;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.secure.common.utils.ThreadUtil;

abstract public class MqMsg {

	protected String msgId;
	protected String srcIP;
	protected String projectName;
	protected String msgClazzName;
	
	public void init(){
		msgId = ThreadUtil.getTraceMsgId();
		
		if(StringUtils.isBlank(this.getSrcIP())){
			this.srcIP = IPUtil.getLocalIp();
		}
		if(StringUtils.isBlank(this.getProjectName())){
			this.projectName = GlobalConstant.PROJECT_NAME;
		}
		
		this.msgClazzName = this.getClass().getName();
	}
	
	public String getMsgClazzName() {
		return msgClazzName;
	}

	public void setMsgClazzName(String msgClazzName) {
		this.msgClazzName = msgClazzName;
	}

	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getSrcIP() {
		return srcIP;
	}
	public void setSrcIP(String srcIP) {
		this.srcIP = srcIP;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}
