package com.ujigu.acl.entity;

import com.ujigu.secure.common.bean.BaseEntity;
import java.util.Date;

public class RoleFunc extends BaseEntity<Integer> {
    private Integer id;

    private Integer roleId;

    private Integer funcId;
    
    private Integer webappId;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getFuncId() {
        return funcId;
    }

    public void setFuncId(Integer funcId) {
        this.funcId = funcId;
    }
    
    public Integer getWebappId() {
		return webappId;
	}

	public void setWebappId(Integer webappId) {
		this.webappId = webappId;
	}

	public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Integer getPK() {
        return id;
    }

}