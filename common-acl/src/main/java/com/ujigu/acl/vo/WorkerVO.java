package com.ujigu.acl.vo;

import com.ujigu.acl.entity.Worker;

public class WorkerVO extends Worker{
	
	private Integer departId;
	private Integer roleId;
	
	public Integer getDepartId() {
		return departId;
	}
	public void setDepartId(Integer departId) {
		this.departId = departId;
	}
	public Integer getRoleId() {
		return roleId;
	}
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	
}
