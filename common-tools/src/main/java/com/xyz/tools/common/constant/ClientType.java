package com.xyz.tools.common.constant;

/**
 * 客户端类型
 * @author lsf
 *
 */
public enum ClientType {
	PC("Windows"),
	H5("MAC"),
	WX_H5("微信H5"), //通过微信浏览器访问的H5，属于H5的一个分支
	Ali_H5("支付宝H5"), //通过支付宝浏览器访问的H5
	Android("华为"),
	IOS("小米"),
	Win_CE("WinCE"),
	WX_APP("微信小程序"),
	Ali_APP("支付宝小程序"),
	Other("Other"),
	;
	
	private String descp;
	
	private ClientType(String descp) {
		this.descp = descp;
	}
	
	public String getDescp(){
		return descp;
	}
	
	public static boolean isH5(ClientType clientType){
		return H5.equals(clientType) || WX_H5.equals(clientType) || Ali_H5.equals(clientType);
	}
	
	public static boolean isApp(ClientType clientType){
		return Android.equals(clientType) || IOS.equals(clientType) || Win_CE.equals(clientType) || WX_APP.equals(clientType) || Ali_APP.equals(clientType);
	}
}
