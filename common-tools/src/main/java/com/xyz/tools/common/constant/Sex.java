package com.xyz.tools.common.constant;

public enum Sex {

	U("未知"),
	M("男"),
	F("女")
	;
	
	private String descp;
	private Sex(String descp){
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	
}
