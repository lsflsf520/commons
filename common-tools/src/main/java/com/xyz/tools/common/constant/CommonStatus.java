package com.xyz.tools.common.constant;

public enum CommonStatus {
	
	NORMAL("正常"), 
	FREEZED("已冻结"),
	INVALID("已失效"),
	CLOSED("已关闭"),
	DELETED("已删除"),
	;

	private String descp;
	private CommonStatus(String descp){
		this.descp = descp;
	}
	
	public String getDescp() {
		return descp;
	}

}
