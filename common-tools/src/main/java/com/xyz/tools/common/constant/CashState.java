package com.xyz.tools.common.constant;

public enum CashState {

	NOSEND("无需发送"),
	FAILED("转账失败"),
	RECEIVED("转账成功"),
	;
	
	private String descp;
	private CashState(String descp){
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	
}
