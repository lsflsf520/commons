package com.yisi.stiku.db.dao;

import java.io.Serializable;
import java.util.List;

import com.yisi.stiku.common.bean.BaseEntity;

public interface BaseDao<PK extends Serializable, T extends BaseEntity<PK>>{

	/**
	 * 
	 * @param pk 
	 * @return 根据主键返回对应的实体对象
	 */
	T findByPK(PK pk);
	
	/**
	 * 
	 * @param t 插入实体
	 */
	void insert(T t);
	
	/**
	 * 
	 * @param t
	 * @return 返回受影响的记录数
	 */
	int updateByPK(T t);
	
	/**
	 * 
	 * @param pk 
	 * @return 根据主键进行删除操作，并返回删除的行数
	 */
	int deleteByPK(PK pk);	
	
	/**
	 * 
	 * @return 返回所有的记录
	 */
	List<T> findAll();
	
}
