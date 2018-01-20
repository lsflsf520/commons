package com.ujigu.secure.common.bean;

public enum MsgType {
	
	SMS("短信"), EMAIL("邮件"), WX("微信公众号"), SITE("站内信");
	
	private String descp;
	
	private MsgType(String descp){
		this.descp = descp;
	}

	public String getDescp() {
		return descp;
	}
	
}
