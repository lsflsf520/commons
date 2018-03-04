package com.xyz.tools.common.bean;

public enum GlobalResultCode implements ResultCodeIntf{

	SUCCESS("成功"),
	SYSTEM_ERROR("系统错误，操作失败，请重试！"),
	DB_OPER_ERROR("数据库操作错误，操作失败，请重试！"),
	PARAM_ERROR("参数错误，请重新进入该页面再重试！"),
	NO_PRIVILEGE("对不起，您没有该权限！"),
	UNKNOWN_ERROR("对不起，服务器发生未知错误，请稍后重试！"),
	FLOW_CONTROL_LIMIT("系统繁忙，请稍后重试！"),
	NON_SECURE_REQUEST("对不起，该请求不安全！"),
	NOT_EXISTS("对不起，数据不存在！"),
	NOT_LOGIN("对不起，请先登录！"),
	ILLEGAL_STATE("对不起，数据状态不可用")
	;
	
	private String errorMsg;
	private GlobalResultCode(String errorMsg){
		this.errorMsg = errorMsg;
	}
	
	@Override
	public String getCode() {
		return this.name();
	}
	@Override
	public String getErrorMsg() {
		return errorMsg;
	}
	@Override
	public String getFormFieldName() {
		return null;
	}
	@Override
	public String getFriendlyMsg() {
		return errorMsg;
	}
	
}
