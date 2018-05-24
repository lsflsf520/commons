package com.xyz.tools.common.constant;

/**
 * 设备类型
 * @author lsf
 *
 */
public enum EquipType {
	WINDOWS("Windows"),
	MAC("MAC"),
	HUAWEI("华为"),
	XIAOMI("小米"),
	IPHONE("IPhone"),
	IPOD("IPOD"),
	IPAD("IPAD"),
	WIN_PHONE("Windows Phone"),
	OTHER("Other"),
	;
	
	private String descp;
	
	private EquipType(String descp) {
		this.descp = descp;
	}
	
	public String getDescp(){
		return descp;
	}
	
}
