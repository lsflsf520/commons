package com.yisi.stiku.db.service.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.data.domain.PageImpl;

import com.yisi.stiku.common.bean.BaseEntity;
import com.yisi.stiku.common.bean.PageInfo;
import com.yisi.stiku.common.bean.PageInfo.OrderDirection;
import com.yisi.stiku.db.dao.impl.BaseDaoImpl;
import com.yisi.stiku.db.service.BaseService;
import com.yisi.stiku.log.LogUtil;

/**
 * 
 * @author shangfeng
 *
 * @param <PK>
 * @param <T>
 */
@SuppressWarnings("all")
abstract public class BaseServiceImpl<PK extends Serializable, T extends BaseEntity<PK>>
		implements BaseService<PK, T> {

	@Override
	public T findById(PK pk) {

		// JedisKeyNS cacheNS = getCacheNameSpace();
		// if (cacheNS != null) {
		// checkJedisKeyType(cacheNS);
		// String valStr = null;
		// if(JedisKeyType.HASH.equals(cacheNS.getKeyType())){
		// valStr = ShardJedisTool.hget(cacheNS,
		// buildKey(pk));
		// }else{
		// valStr = ShardJedisTool.get(cacheNS, buildKey(pk));
		// }
		// if (StringUtils.isNotBlank(valStr)) {
		// return new Gson().fromJson(valStr,
		// TypeToken.of(getGenericType(1)).getType());
		// }
		// }

		// T t = getBaseDaoImpl().findByPK(pk);

		// if (t != null && cacheNS != null) {
		// if(JedisKeyType.HASH.equals(cacheNS.getKeyType())){
		// ShardJedisTool.hset(cacheNS, buildKey(pk),
		// new Gson().toJson(t, TypeToken.of(t.getClass()).getType()));
		// }else{
		// ShardJedisTool.set(cacheNS, buildKey(pk),
		// new Gson().toJson(t, TypeToken.of(t.getClass()).getType()));
		// }
		// }

		// return t;
		return getBaseDaoImpl().findByPK(pk);
	}

	public T findById(PK pk, boolean forceMaster) {

		return getBaseDaoImpl().findByPK(pk, forceMaster);
	}

	/**
	 * 检查jedis的key是否合法
	 * 
	 * @param cacheNS
	 */
	// private void checkJedisKeyType(JedisKeyNS cacheNS){
	// if(cacheNS.needRemoveAllCacheAfterModify() &&
	// !JedisKeyType.HASH.equals(cacheNS.getKeyType())){
	// throw new BaseRuntimeException("NOT_SUPPORT", "系统异常，请联系管理员！",
	// "jedis key " + cacheNS +
	// "'s type is not supported by needRemoveAllCacheAfterModify");
	// }
	// }

	@Override
	public void insert(T t) {

		long start = System.currentTimeMillis();

		getBaseDaoImpl().insert(t);

		if (needOperLog()) {
			LogUtil.operLog(t);
		}

		// JedisKeyNS cacheNamespace = getCacheNameSpace();
		// if (cacheNamespace != null
		// && cacheNamespace.needRemoveAllCacheAfterModify()) {
		// ShardJedisTool.delHKey(cacheNamespace);
		// }

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".insert", System.currentTimeMillis() - start);
	}

	@Override
	public PK saveEntity(T t) {

		if (t.getPK() == null) {
			return this.insertReturnPK(t);
		}

		this.update(t);

		return t.getPK();
	}

	@Override
	public void insertBatch(List<T> tList) {

		long start = System.currentTimeMillis();
		// JedisKeyNS cacheNamespace = getCacheNameSpace();
		// if (cacheNamespace != null
		// && cacheNamespace.needRemoveAllCacheAfterModify()) {
		// ShardJedisTool.delHKey(cacheNamespace);
		// }

		getBaseDaoImpl().insertBatch(tList);

		if (needOperLog()) {
			LogUtil.operLog(tList);
		}
		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".insertBatch", System.currentTimeMillis() - start);
	}

	// @Override
	// public void insertWithChildren(T t,
	// List<ChildBaseEntity> children) {
	// this.insert(t);
	//
	// if(t.getPK() != null){
	// for(ChildBaseEntity<? extends Serializable> child : children){
	// child.setParentId(t.getPK());
	// }
	//
	// // getChildDaoImpl().insertBatch(children);
	//
	// return;
	// }
	// // throw new Sq
	// }

	@Override
	public PK insertReturnPK(T t) {

		long start = System.currentTimeMillis();
		// JedisKeyNS cacheNamespace = getCacheNameSpace();
		// if (cacheNamespace != null) {
		// if (cacheNamespace.needRemoveAllCacheAfterModify()) {
		// ShardJedisTool.delHKey(cacheNamespace);
		// } else if(JedisKeyType.HASH.equals(cacheNamespace.getKeyType())){
		// ShardJedisTool.hset(cacheNamespace, buildKey(t.getPK()),
		// new Gson().toJson(t, TypeToken.of(t.getClass())
		// .getType()));
		// }else{
		// ShardJedisTool.set(cacheNamespace, buildKey(t.getPK()), new
		// Gson().toJson(t, TypeToken.of(t.getClass())
		// .getType()));
		// }
		// }

		getBaseDaoImpl().insertReturnPK(t);

		if (needOperLog()) {
			LogUtil.operLog(t);
		}
		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".insertReturnPK", System.currentTimeMillis() - start);

		return t.getPK();
	}

	@Override
	public boolean update(T t) {

		long start = System.currentTimeMillis();

		// JedisKeyNS cacheNamespace = getCacheNameSpace();
		// if (cacheNamespace != null) {
		// if (cacheNamespace.needRemoveAllCacheAfterModify()) {
		// ShardJedisTool.delHKey(cacheNamespace);
		// } else if(JedisKeyType.HASH.equals(cacheNamespace.getKeyType())){
		// ShardJedisTool.hset(cacheNamespace, buildKey(t.getPK()),
		// new Gson().toJson(t, TypeToken.of(t.getClass())
		// .getType()));
		// } else {
		// ShardJedisTool.set(cacheNamespace, buildKey(t.getPK()), new
		// Gson().toJson(t, TypeToken.of(t.getClass())
		// .getType()));
		// }
		// }

		T preT = null;
		if (needOperLog()) {
			preT = findById(t.getPK());
		}

		int effectRows = getBaseDaoImpl().updateByPK(t);

		if (needOperLog()) {
			LogUtil.operLog(preT, t);
		}
		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".update", System.currentTimeMillis() - start);

		return effectRows >= 0;
	}

	@Override
	public int deleteById(PK pk) {

		long start = System.currentTimeMillis();

		// JedisKeyNS cacheNamespace = getCacheNameSpace();
		// if (cacheNamespace != null) {
		// if (cacheNamespace.needRemoveAllCacheAfterModify()) {
		// ShardJedisTool.delHKey(cacheNamespace);
		// } else if(JedisKeyType.HASH.equals(cacheNamespace.getKeyType())){
		// ShardJedisTool.hdel(cacheNamespace, buildKey(pk));
		// } else {
		// ShardJedisTool.del(cacheNamespace, buildKey(pk));
		// }
		// }

		int effectRows = getBaseDaoImpl().deleteByPK(pk);

		if (needOperLog()) {
			LogUtil.operLog(pk);
		}
		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".update", System.currentTimeMillis() - start);

		return effectRows;
	}

	@Override
	public List<T> findAll() {

		long start = System.currentTimeMillis();

		List<T> tList = getBaseDaoImpl().findAll();

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".findAll", System.currentTimeMillis() - start);
		return tList;
	}

	@Override
	public List<T> findAll(String columnName, OrderDirection direction) {

		return findAll(" order by " + columnName + " " + direction);
	}

	@Override
	public List<T> findAll(String orderBySql) {

		long start = System.currentTimeMillis();

		List<T> tlist = getBaseDaoImpl().findAll(orderBySql, false);

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".findAll", System.currentTimeMillis() - start);
		return tlist;
	}

	@Override
	public List<T> findByEntity(T t) {

		return findByEntity(t, null);
	}

	@Override
	public List<T> findByEntity(T t, String orderBySql) {

		return findByEntity(t, orderBySql, false);
	}

	@Override
	public List<T> findByEntity(T t, String orderBySql, boolean forceMaster) {

		long start = System.currentTimeMillis();

		List<T> tList = getBaseDaoImpl().findByEntity(t, orderBySql, forceMaster);

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".findByEntity", System.currentTimeMillis() - start);
		return tList;
	}

	@Override
	public List<T> findByEntity(T t, String orderField, OrderDirection direction) {

		String orderBySql = " order by " + orderField + " " + direction + " ";

		return findByEntity(t, orderBySql);
	}

	@Override
	public T findOneByEntity(T t) {

		List<T> tList = findByEntity(t);
		return tList == null || tList.isEmpty() ? null : tList.get(0);
	}

	@Override
	public T findOneByEntity(T t, String fieldName, OrderDirection direction) {

		List<T> tList = findByEntity(t, fieldName, direction);
		return tList == null || tList.isEmpty() ? null : tList.get(0);
	}

	@Override
	public T findOneByEntity(T t, String orderBySql) {

		return findOneByEntity(t, orderBySql, false);
	}

	@Override
	public T findOneByEntity(T t, String orderBySql, boolean forceMaster) {

		List<T> tList = findByEntity(t, orderBySql, forceMaster);
		return tList == null || tList.isEmpty() ? null : tList.get(0);
	}

	@Override
	public PageImpl<T> findByPage(T t, int currPage, int maxRows,
			String orderField, OrderDirection direction) {

		long start = System.currentTimeMillis();

		PageImpl<T> pager = findByPage(t, currPage, maxRows, " order by "
				+ orderField + " " + direction + " ");

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".findByPage", System.currentTimeMillis() - start);
		return pager;
	}

	@Override
	public PageImpl<T> findByPage(T t, int currPage, int maxRows,
			String orderSql) {

		long start = System.currentTimeMillis();
		PageInfo pageInfo = new PageInfo(currPage, maxRows);
		pageInfo.setOrderBySql(orderSql);

		PageImpl<T> pager = getBaseDaoImpl().findByPage(t, pageInfo, null);

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".findByPage", System.currentTimeMillis() - start);
		return pager;
	}

	@Override
	public PageImpl<T> findByPage(T t, int currPage, int maxRows) {

		return findByPage(t, currPage, maxRows, null);
	}

	@Override
	public PageImpl<T> findByPage(T t, int currPage, int maxRows,
			String orderSql, String dynamicSql) {

		long start = System.currentTimeMillis();
		PageInfo pageInfo = new PageInfo(currPage, maxRows);
		pageInfo.setOrderBySql(orderSql);

		PageImpl<T> pager = getBaseDaoImpl()
				.findByPage(t, pageInfo, dynamicSql);

		LogUtil.xnLog(LogUtil.NORMAL_TYPE, this.getClass().getName()
				+ ".findByPage", System.currentTimeMillis() - start);

		return pager;
	}

	@Override
	public PageImpl<T> findByPage(T t, int currPage, int maxRows,
			String orderField, OrderDirection direction, String dynamicSql) {

		return findByPage(t, currPage, maxRows, " order by " + orderField + " "
				+ direction + " ", dynamicSql);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	protected String buildKey(Serializable key) {

		return "d" + key;
	}

	/**
	 * 
	 * @return 如果需要dao层自动帮助缓存PK为键、Entity为值的键值对，则子类需要重写此方法
	 */
	// protected JedisKeyNS getCacheNameSpace() {
	//
	// return null;
	// }

	/**
	 * 
	 * @return 返回true(默认)，记录操作日志；否则不记录操作日志
	 */
	protected boolean needOperLog() {

		return true;
	}

	/**
	 * \r 55 \r 56
	 * 
	 * @param index
	 *            \r 57
	 * @return 返回泛型的类型\r 58
	 */
	public Class getGenericType(int index) {

		Type genType = getClass().getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			throw new RuntimeException("Index outof bounds");
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class) params[index];
	}

	abstract protected BaseDaoImpl<PK, T> getBaseDaoImpl();

	// protected BaseDaoImpl<? extends Serializable, ? extends ChildBaseEntity<?
	// extends Serializable>> getChildDaoImpl(){
	// return null;
	// }

}
