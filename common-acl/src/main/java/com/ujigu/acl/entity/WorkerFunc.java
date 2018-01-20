package com.ujigu.acl.entity;

import com.ujigu.secure.common.bean.BaseEntity;
import java.util.Date;

public class WorkerFunc extends BaseEntity<Integer> {
    private Integer id;

    private Integer workerId;

    private Integer funcId;
    
    private Integer webappId;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Integer workerId) {
        this.workerId = workerId;
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