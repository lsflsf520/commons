package com.ujigu.secure.common.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PKBean<PK extends Serializable> {

	/**
	 * 
	 * @return 返回实体类的主键
	 */
	@JsonIgnore
	PK getPK();
	
}
