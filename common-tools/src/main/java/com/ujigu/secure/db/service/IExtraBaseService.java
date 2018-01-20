package com.ujigu.secure.db.service;

import java.io.Serializable;

import com.ujigu.secure.common.bean.BaseEntity;

public interface IExtraBaseService <PK extends Serializable, T extends BaseEntity<PK>> {

	/**
	 * 
	 * @param t
	 * @return 插入对象到数据库，并返回主键值
	 */
	PK insertReturnPK(T t);
	
	/**
	 * 
	 * @param t
	 * @return 保存(数据已存在则更新，否则插入)对象到数据库，并返回主键值
	 */
	PK doSave(T t);
	
}
