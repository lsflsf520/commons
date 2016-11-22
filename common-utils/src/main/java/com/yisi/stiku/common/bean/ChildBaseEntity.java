package com.yisi.stiku.common.bean;

import java.io.Serializable;

abstract public class ChildBaseEntity<PK extends Serializable> extends BaseEntity<PK> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see 
	 * @param parentId 实体对象的父类，当需要进行批量级联insert的时候需要
	 * 
	 */
	abstract public void setParentId(Serializable parentId);

}
