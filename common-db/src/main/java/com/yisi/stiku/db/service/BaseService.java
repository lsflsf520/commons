package com.yisi.stiku.db.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.PageImpl;

import com.yisi.stiku.common.bean.BaseEntity;
import com.yisi.stiku.common.bean.PageInfo.OrderDirection;

public interface BaseService<PK extends Serializable, T extends BaseEntity<PK>> {

	T findById(PK pk);
	
	void insert(T t);
	
	/**
	 * 如果实体的主键存在，则执行更新操作，否则执行插入操作
	 * @param t
	 * @return 返回实体的主键
	 */
	PK saveEntity(T t);
	
	PK insertReturnPK(T t);
	
	void insertBatch(List<T> tList);
	
//	void insertWithChildren( T t, List<ChildBaseEntity<? extends Serializable>> children);
	
	boolean update(T t);
	
	int deleteById(PK pk);
	
	List<T> findAll();
	
	List<T> findAll(String columnName, OrderDirection direction);
	
	List<T> findAll(String orderBySql);
	
	List<T> findByEntity(T t);
	
	List<T> findByEntity(T t, String orderBySql, boolean forceMaster);
	
	List<T> findByEntity(T t, String orderBySql);
	
	List<T> findByEntity(T t, String columnName, OrderDirection direction);
	
	T findOneByEntity(T t);
	
	T findOneByEntity(T t, String fieldName, OrderDirection direction);
	
	T findOneByEntity(T t, String orderBySql, boolean forceMaster);
	
	T findOneByEntity(T t, String orderBySql);
	
	PageImpl<T> findByPage(T t, int currPage, int maxRows, String orderField, OrderDirection direction);
	
	PageImpl<T> findByPage(T t, int currPage, int maxRows, String orderSql);
	
	PageImpl<T> findByPage(T t, int currPage, int maxRows,
			String orderSql, String dynamicSql);
	
	PageImpl<T> findByPage(T t, int currPage, int maxRows,
			String orderField, OrderDirection direction, String dynamicSql);
	
	PageImpl<T> findByPage(T t, int currPage, int maxRows);
	
}
