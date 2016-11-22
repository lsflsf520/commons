package com.yisi.stiku.common.checker.constant;

public enum RequiredType {

	ALL("插入和更新都不能为空"),
	NONE("字段非必须"),
	INSERT("插入不能为空"),
	UPDATE("更新不能为空")
	;
	
	private String desc;
	private RequiredType(String desc){
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
	}
	
}
