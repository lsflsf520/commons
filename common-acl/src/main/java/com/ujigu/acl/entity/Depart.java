package com.ujigu.acl.entity;

import com.ujigu.secure.common.bean.AbstractTreeBean;
import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.TreeBean;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Depart extends AbstractTreeBean<Integer, Depart>{
    private Integer id;

    private String name;

    private Integer parentId;

    private Integer priority;

    private CommonStatus status;

    private Date createTime;

    private Date lastUptime;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public CommonStatus getStatus() {
        return status;
    }

    public void setStatus(CommonStatus status) {
        this.status = status;
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