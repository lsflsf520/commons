package com.ujigu.csai.msglog.constant;

public enum MsgStatus {
	
	SUCCESS("成功"), CONFIRM("待确认"), FAIL("失败");
	
	private String descp;
	
	private MsgStatus(String descp){
		this.descp = descp;
	}

	public String getDescp() {
		return descp;
	}
	
}
