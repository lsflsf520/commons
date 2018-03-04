package com.xyz.tools.common.bean;

public enum Bool {

	U(-1, "未知"),
	Y(1, "是"),
	N(0, "否")
	;
	private int code;
	private String descp;
	private Bool(int code, String descp){
		this.code = code;
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	public int getCode() {
		return code;
	}
	
}
