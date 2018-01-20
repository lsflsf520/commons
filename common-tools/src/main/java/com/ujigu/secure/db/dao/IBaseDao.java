package com.ujigu.secure.db.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.ujigu.secure.common.bean.BaseEntity;

public interface IBaseDao<PK extends Serializable, T extends BaseEntity<PK>>{

	/**
	 * 
	 * @param pk 
	 * @return 根据主键返回对应的实体对象
	 */
	T findByPK(PK pk);
	
	/**
	 * 多主键查询
	 * @param pks 
	 * @return
	 */
	List<T> findByPks(@Param("pks") PK... pks);
	
	/**
	 * 
	 * @param t 插入实体
	 */
	void insert(T t);
	
	/**
	 * 
	 * @param tList 批量插入实体
	 */
	void insertBatch(List<T> tList);
	
	/**
	 * 
	 * @param t
	 * @return 返回受影响的记录数
	 */
	int updateByPK(T t);
	
	/**
	 * 对数据进行软删，service在调用该方法之前，需要先对statusColName参数进行防sql注入校验
	 * @param statusColName 状态列的列名
	 * @delStatusVal delStatusVal 软删的状态值
	 * @param pks 需要被软删的主键数组
	 */
	void updateStatus(@Param("cond") T t, @Param("statusColName") String statusColName, @Param("delStatusVal") String delStatusVal, @Param("pks") PK... pks);
	
	/**
	 * 
	 * @param pk 
	 * @return 根据主键进行删除操作，并返回删除的行数
	 */
	int deleteByPK(@Param("cond") T t, @Param("id") PK pk);	
	
	/**
	 * 根据主键批量删除数据
	 * @param pks 
	 */
	void batchDel(@Param("cond") T t, @Param("pks") PK... pks);
	
	/**
	 * 
	 * @param t 
	 * @param pageBounds 
	 * @return 返回所有的记录
	 */
	List<T> findAll(T t, PageBounds pageBounds);
	
}
