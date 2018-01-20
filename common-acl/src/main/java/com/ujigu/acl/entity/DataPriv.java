package com.ujigu.acl.entity;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.utils.StringUtil;

public class DataPriv extends BaseEntity<Integer> {
    private Integer id;

    private Integer workerId;

    private String loader;

//    private String workerData;
    private Set<String> workerDatas;

    private Date createTime;

    private Date lastUptime;

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

    public String getLoader() {
		return loader;
	}

	public void setLoader(String loader) {
		this.loader = loader == null ? null : loader.trim();
	}

	public String getWorkerData() {
        return StringUtils.join(this.workerDatas, ",");
    }

    public void setWorkerData(String workerData) {
//        this.workerData = workerData == null ? null : workerData.trim();
    	this.workerDatas = StringUtil.toSet(workerData);
    }
    
    public Set<String> getWorkerDatas() {
		return workerDatas;
	}

	public void setWorkerDatas(Set<String> workerDatas) {
		this.workerDatas = workerDatas;
	}

	public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUptime() {
        return lastUptime;
    }

    public void setLastUptime(Date lastUptime) {
        this.lastUptime = lastUptime;
    }

    @Override
    public Integer getPK() {
        return id;
    }
}