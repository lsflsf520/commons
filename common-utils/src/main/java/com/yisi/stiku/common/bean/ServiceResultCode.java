package com.yisi.stiku.common.bean;

public class ServiceResultCode implements ResultCodeIntf {
	
	private String code;
	private String errorMsg; //用于输出详细的错误日志到服务器
	private String friendlyMsg; //返回给客户端显示用的友好提示信息
	private String formFieldName; //如果有表单提交，则可以设置该值以告知客户端是具体哪个字段的校验发生错误

	public ServiceResultCode(String code, String friendlyMsg) {
		this(code, friendlyMsg, friendlyMsg, null);
	}
	
	public ServiceResultCode(String code, String friendlyMsg,String errorMsg) {
		this(code, friendlyMsg, errorMsg, null);
	}
	
	public ServiceResultCode(String code, String friendlyMsg, String errorMsg, String formFieldName) {
		this.code = code;
		this.friendlyMsg = friendlyMsg;
		this.errorMsg = errorMsg;
		this.formFieldName = formFieldName;
	}
	
	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getErrorMsg() {
		return errorMsg;
	}
	
	@Override
	public String getFriendlyMsg() {
		return friendlyMsg;
	}

	@Override
	public String getFormFieldName() {
		return formFieldName;
	}

	@Override
	public String toString() {
		return "code:" + code + ",errorMsg:" + errorMsg;
	}
}
