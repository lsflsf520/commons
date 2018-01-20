package com.ujigu.secure.common.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/3/9.
 */
public abstract class OperatorEntity<PK extends Serializable> extends BaseEntity<PK> {
    public abstract Integer getCreateBy();

    public abstract void setCreateBy(Integer createBy);

    public abstract Integer getUpdateBy();

    public abstract void setUpdateBy(Integer updateBy);

    public abstract Date getUpdateTime();

    public abstract void setUpdateTime(Date updateTime);

    public abstract Date getCreateTime();

    public abstract void setCreateTime(Date createTime);
}
