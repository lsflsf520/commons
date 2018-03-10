package com.xyz.tools.common.constant;

public enum PayMethod {

	OFFLINE("线下支付"),
	BALANCE("余额支付"),
	WXPAY("微信支付"),
	ALIPAY("支付宝"),
	UNIONPAY("银联支付"),
	;
	
	private String descp;
	private PayMethod(String descp){
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}
	
}
