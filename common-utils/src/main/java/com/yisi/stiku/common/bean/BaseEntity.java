package com.yisi.stiku.common.bean;

import java.io.Serializable;

public abstract class BaseEntity<PK extends Serializable> implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @return 返回实体类的主键
	 */
	abstract public PK getPK();
	
	/**
	 * 
	 */
	public void setPK(PK pk){
		
	}
	
}
