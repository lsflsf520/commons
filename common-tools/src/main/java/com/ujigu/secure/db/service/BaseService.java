package com.ujigu.secure.db.service;

import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.bean.PageInfo.OrderDirection;
import org.springframework.data.domain.PageImpl;

import java.io.Serializable;
import java.util.List;

public interface BaseService<PK extends Serializable, T extends BaseEntity<PK>> {

	T findById(PK pk);

	T load(PK pk) ;
	
	void insert(T t);
	
	/**
	 * 如果实体的主键存在，则执行更新操作，否则执行插入操作
	 * @param t
	 * @return 返回实体的主键O
	 */
	PK saveEntity(T t);
	
	PK insertReturnPK(T t);
	
	void insertBatch(List<T> tList);
	
//	void insertWithChildren( T t, List<ChildBaseEntity<? extends Serializable>> children);
	
	boolean update(T t);
	
	int deleteById(PK pk);

	int batchDelete(List<PK> pks);

	List<T> findAll();
	
	List<T> findAll(String columnName, OrderDirection direction);
	
	List<T> findAll(String fieldAndOrder);
	
	List<T> findByEntity(T t);
	
	List<T> findByEntity(T t, String columnName, OrderDirection direction);
	
	List<T> findByEntity(T t, String fieldAndOrder);
	
	T findOneByEntity(T t);
	
	T findOneByEntity(T t, String fieldName, OrderDirection direction);
	
	PageImpl<T> findByPage(T t, Integer currPage, Integer maxRows, String orderField, OrderDirection direction);
	
	/**
	 * 
	 * @param t
	 * @param currPage
	 * @param maxRows
	 * @param fieldAndOrder 多字段排序组合而成的字符串，比如 "name asc, age desc"
	 * @return
	 */
	PageImpl<T> findByPage(T t, Integer currPage, Integer maxRows, String fieldAndOrder);
	
	PageImpl<T> findByPage(T t, Integer currPage, Integer maxRows);
	
}
