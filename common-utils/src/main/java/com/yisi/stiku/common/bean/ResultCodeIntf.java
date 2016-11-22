package com.yisi.stiku.common.bean;

public interface ResultCodeIntf {

	/**
	 * 
	 * @return 错误码或者异常码
	 */
	public String getCode();
	
	/**
	 * 
	 * @return 返回给终端用户的消息提示(比如使用浏览器来访问的用户)
	 */
	public String getFriendlyMsg();
	
	/**
	 * 
	 * @return 返回程序员能看懂的消息提示，以便查找原因
	 */
	
	public String getErrorMsg();
	
	/**
	 * 
	 * @return 如果是表单提交，则会对应表单中的某个字段的名称
	 */
	public String getFormFieldName();
	
}
