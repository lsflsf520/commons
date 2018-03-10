package com.xyz.tools.common.constant;

public enum OrdState {

	CANCEL("已取消"),
	INVALID("已失效"),
	PAYING("待支付"),
	PAYED("已支付"),
	;
	
	private String descp;
	private OrdState(String descp){
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	
}
