package com.yisi.stiku.db.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.domain.PageImpl;

import com.yisi.stiku.common.bean.BaseEntity;
import com.yisi.stiku.common.bean.PageInfo;
import com.yisi.stiku.common.bean.PageInfo.OrderDirection;
import com.yisi.stiku.db.dao.BaseDao;
import com.yisi.stiku.db.multi.MSDSKeyHolder;
import com.yisi.stiku.db.multi.SqlSessionTemplateFactory;

/**
 * 
 * @author shangfeng
 *
 * @param <PK>
 * @param <T>
 */
abstract public class BaseDaoImpl<PK extends Serializable, T extends BaseEntity<PK>> implements BaseDao<PK, T> {

	@Resource
	private SqlSessionTemplateFactory sqlSessionTemplateFactory;

	/**
	 * 
	 * @param t
	 * @param pageInfo
	 * @param dynamicSql
	 * @return
	 */
	public PageImpl<T> findByPage(T t, PageInfo pageInfo, String dynamicSql) {

		return findByPage(t, pageInfo, dynamicSql, false);
	}

	/**
	 * 
	 * @param t
	 * @param pageInfo
	 * @param dynamicSql
	 * @param forceMaster
	 *            该参数为true，将强制走主库
	 * @return
	 */
	public PageImpl<T> findByPage(T t, PageInfo pageInfo, String dynamicSql, boolean forceMaster) {

		String namespace = getMapperNamespace();

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MSDSKeyHolder.FORCE_MASTER, forceMaster);
		paramMap.put("entity", t);
		paramMap.put("pageInfo", pageInfo);
		paramMap.put("dynamicSql", dynamicSql);

		List<Integer> count = getSqlSessionTemplate().selectList(namespace + ".getCntByPage", paramMap);
		if (count != null && !count.isEmpty()) {
			pageInfo.setTotalCounts(count.get(0));
		}

		List<T> tList = getSqlSessionTemplate().selectList(namespace + ".findByPage", paramMap);
		if (tList == null || tList.isEmpty()) {
			return new PageImpl<T>(new ArrayList<T>());
		}

		PageImpl<T> pager = new PageImpl<T>(tList, pageInfo, pageInfo.getTotalCounts());

		return pager;
	}

	/**
	 * 
	 * @param t
	 * @return 插入一个实体，并返回其主键
	 */
	public PK insertReturnPK(T t) {

		getSqlSessionTemplate().insert(getMapperNamespace() + ".insertReturnPK", t);

		return t.getPK();
	}

	/**
	 * 
	 * @param tList
	 *            需要被插入到数据库中的对象列表
	 */
	public void insertBatch(List<T> list) {

		getSqlSessionTemplate().insert(getMapperNamespace() + ".insertBatch", list);
	}

	public List<T> findByEntity(T t, String orderBySql) {

		return findByEntity(t, orderBySql, false);
	}

	/**
	 * 
	 * @param t
	 *            携带查询属性的实体对象
	 * @param orderBySql
	 *            排序sql
	 * @return 返回符合条件的所有实体对象
	 */
	@SuppressWarnings("all")
	public List<T> findByEntity(T t, String orderBySql, boolean forceMaster) {

		String namespace = getMapperNamespace();

		if (t.getPK() != null) {
			T cacheEntity = findByPK(t.getPK());
			if (cacheEntity != null) {
				return Arrays.asList(cacheEntity);
			}
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MSDSKeyHolder.FORCE_MASTER, forceMaster);
		paramMap.put("entity", t);
		paramMap.put("orderBySql", orderBySql);

		return getSqlSessionTemplate().selectList(namespace + ".findByEntity", paramMap);
	}

	/**
	 * 
	 * @param t
	 *            携带查询属性的实体对象
	 * @return 返回符合条件的所有实体对象
	 */
	public List<T> findByEntity(T t) {

		return this.findByEntity(t, null);
	}

	/**
	 * 
	 * @param t
	 *            携带查询属性的实体对象
	 * @return 内部调用findByEntity方法，但只取出其中的第一个对象返回
	 */
	public T findOneByEntity(T t) {

		return findOneByEntity(t, false);
	}

	/**
	 * 
	 * @param t
	 * @param forceMaster
	 *            该参数为true，则强制走主库
	 * @return
	 */
	public T findOneByEntity(T t, boolean forceMaster) {

		List<T> entityList = findByEntity(t, null, forceMaster);

		return entityList == null || entityList.isEmpty() ? null : entityList.get(0);
	}

	public T findByPK(PK pk) {

		// return this.getSqlSessionTemplate().selectOne(getMapperNamespace() +
		// ".findByPK", pk);
		return this.getProxyBaseDao().findByPK(pk);
	}

	/**
	 * 
	 * @param pk
	 * @param forceMaster
	 *            该参数为true，则强制走主库
	 * @return
	 */
	public T findByPK(PK pk, boolean forceMaster) {

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("pk", pk);
		paramMap.put(MSDSKeyHolder.FORCE_MASTER, forceMaster);

		return this.getSqlSessionTemplate().selectOne(getMapperNamespace() + ".findByPK_inMaster", paramMap);
		// return this.getProxyBaseDao().findByPK(pk);

	}

	/**
	 * 
	 * @param t
	 *            插入实体
	 */
	public void insert(T t) {

		// getProxyBaseDao().insert(t);
		this.getSqlSessionTemplate().insert(getMapperNamespace() + ".insert", t);
	}

	/**
	 * 
	 * @param t
	 * @return 返回受影响的记录数
	 */
	public int updateByPK(T t) {

		// return getProxyBaseDao().updateByPK(t);
		return this.getSqlSessionTemplate().update(getMapperNamespace() + ".updateByPK", t);
	}

	/**
	 * 
	 * @param pk
	 * @return 根据主键进行删除操作，并返回删除的行数
	 */
	public int deleteByPK(PK pk) {

		// return getProxyBaseDao().deleteByPK(pk);
		return this.getSqlSessionTemplate().delete(getMapperNamespace() + ".deleteByPK", pk);
	}

	/**
	 * 
	 * @return 返回所有的记录
	 */
	public List<T> findAll() {

		return findAll(false);
	}

	/**
	 * 
	 * @param forceMaster
	 *            该参数为true，则强制走主库
	 * @return
	 */
	public List<T> findAll(boolean forceMaster) {

		return findAll(null, forceMaster);
	}

	public List<T> findAll(String columnName, OrderDirection direction) {

		return findAll(" order by " + columnName + " " + direction + " ", false);
	}

	/**
	 * 
	 * @param orderBySql
	 * @param forceMaster
	 *            该参数为true，则强制走主库
	 * @return
	 */
	public List<T> findAll(String orderBySql, boolean forceMaster) {

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orderBySql", orderBySql);
		paramMap.put(MSDSKeyHolder.FORCE_MASTER, forceMaster);

		List<T> tlist = this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".findAll", paramMap);

		return tlist;
	}

	/**
	 * 
	 * @return 
	 *         如果该方法返回空，那么系统会默认会使用子类的类名(如果类名以ExtDaoImpl结尾，则会将ExtDaoImpl替换成Dao结尾，并且把包路径
	 *         .dao.impl.替换成.dao.)作为mapper文件的命名空间
	 */
	protected String getMapperNamespace() {

		String namespace = this.getClass().getName();
		if (namespace.endsWith("DaoImpl")) {
			namespace = namespace.replace("DaoImpl", "Dao");
			if (namespace.contains(".dao.impl.")) {
				namespace = namespace.replace(".dao.impl.", ".dao.");
			}
		}

		return namespace;
	}

	protected SqlSessionTemplate getSqlSessionTemplate() {

		return sqlSessionTemplateFactory.getSqlSessionTemplate(this.getClass().getName());
	}

	abstract protected BaseDao<PK, T> getProxyBaseDao();

}
