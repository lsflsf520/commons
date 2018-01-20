package com.ujigu.acl.constant;

public enum RoleEnum {
	
	SUPER_ADMIN(1, "超级管理员"),
	DEPART_ADMIN(2, "公司管理员"),
	ANON(3, "匿名用户");

	private int code;
	private String descp;
	private RoleEnum(int code, String descp){
		this.code = code;
		this.descp = descp;
	}
	public String getDescp() {
		return descp;
	}

	public int getCode() {
		return code;
	}
	public boolean isSuperAdmin(int roleId){
		return SUPER_ADMIN.getCode() == roleId;
	}
	
	public boolean isDepartAdmin(int roleId){
		return DEPART_ADMIN.getCode() == roleId;
	}
	
}
