package com.xyz.tools.common.constant;

public enum CheckState {

	DELETED("已删除"),
	CLOSED("已关闭"),
	REJECT("审核失败"),
	CHECKING("待审核"),
	PASSED("已审核"),
	;
	
	private String descp;
	private CheckState(String descp){
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	
}
