package com.xyz.tools.common.constant;

public enum CheckState {

	DELETED("已删除"),
	CLOSED("已关闭"),
	OFFLINE("已下架"),
	EXPIRED("已失效"),
	REJECT("审核失败"),
	CHECKING("待审核"),
	PASSED("已审核"),
	ONLINE("已上架"),
	;
	
	private String descp;
	private CheckState(String descp){
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	
}
