package com.ujigu.secure.db.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.CollectionUtils;

import com.github.miemiedev.mybatis.paginator.domain.Order;
import com.github.miemiedev.mybatis.paginator.domain.Order.Direction;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.db.dao.IBaseDao;

/**
 * 
 * @author shangfeng
 *
 * @param <PK>
 * @param <T>
 */
@SuppressWarnings("all")
abstract public class AbstractBaseService<PK extends Serializable, T extends BaseEntity<PK>> implements IExtraBaseService<PK, T>{
	
	abstract protected IBaseDao<PK, T> getBaseDao();
	
	/**
	 * 
	 * @param pk 主键
	 * @return 返回主键对应的数据对象
	 */
	public T findById(PK pk) {
		if(pk==null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "findById方法的主键 pk不能为null");
		}
		
		return getBaseDao().findByPK(pk);
//		List<T> tlist = getBaseDao().findByPK(pk);
		
//		return CollectionUtils.isEmpty(tlist) ? null : tlist.get(0);
	}
	
	public List<T> findByIds(PK... pks){
		if(pks == null || pks.length <= 0){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "findById方法的主键 pk不能为null");
		}
		
		return getBaseDao().findByPks(pks);
	}

	/**
	 * 
	 * @param t 插入数据对象
	 */
	public void insert(T t) {
		if(t == null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "insert方法的数据对象不能为空");
		}

		getBaseDao().insert(t);
	}
	
	public void insertBatch(List<T> tList) {
		if(CollectionUtils.isEmpty(tList)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "insertBatch方法的数据对象不能为空");
		}

		getBaseDao().insertBatch(tList);
	}
	
	/**
	 * 
	 * @param t
	 * @return 插入对象到数据库，并返回主键值
	 */
	@Override
	public PK insertReturnPK(T t){
		throw new BaseRuntimeException("NOT_IMPLEMENTED", "不支持的操作", "not implement by sub class " + this.getClass().getName() );
	}
	
	/**
	 * 
	 * @param t
	 * @return 保存(数据已存在则更新，否则插入)对象到数据库，并返回主键值
	 */
	@Override
	public PK doSave(T t){
		throw new BaseRuntimeException("NOT_IMPLEMENTED", "不支持的操作", "not implement by sub class " + this.getClass().getName() );
	}

	public boolean update(T t) {
		if(t == null || t.getPK() == null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "update方法的数据对象及其主键均不能为空");
		}

		int effectRows = getBaseDao().updateByPK(t);

		return effectRows >= 0;
	}

	public int deleteById(PK pk) {
		if(pk==null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "deleteById方法的主键 pk不能为null");
		}
		
		int effectRows = getBaseDao().deleteByPK(null, pk);

		return effectRows;
	}

	public boolean batchDel(PK... pks) {
		if(pks == null || pks.length <= 0) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "batchDel的参数不能为空");
		}

		getBaseDao().batchDel(null, pks);

		return true;
	}
	
	/**
	 * 对数据进行软删。默认是将表中的status字段修改为DELETED；如果表不是这样的规则代表软删，请子类重写该方法
	 * @param pks
	 * @return
	 */
	public boolean softDel(PK... pks){
		if(pks == null || pks.length <= 0) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "softDel的参数不能为空");
		}
		
		getBaseDao().updateStatus(null, "status", CommonStatus.DELETED.name(), pks);
		
		return true;
	}
	
	/**
	 * 
	 * @param pks 
	 * @return
	 */
	public boolean invalid(PK... pks){
		if(pks == null || pks.length <= 0) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "invalid的参数不能为空");
		}
		
		getBaseDao().updateStatus(null, "status", CommonStatus.INVALID.name(), pks);
		
		return true;
	}
	
	/**
	 * 
	 * @param pks 
	 * @return
	 */
	public boolean close(PK... pks){
		if(pks == null || pks.length <= 0) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "close的参数不能为空");
		}
		
		getBaseDao().updateStatus(null, "status", CommonStatus.CLOSED.name(), pks);
		
		return true;
	}
	
	/**
	 * 
	 * @param pks 
	 * @return
	 */
	public boolean freeze(PK... pks){
		if(pks == null || pks.length <= 0) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "freeze的参数不能为空");
		}
		
		getBaseDao().updateStatus(null, "status", CommonStatus.FREEZE.name(), pks);
		
		return true;
	}
	
	/**
	 * 将数据的状态更新为正常可用。默认是将表中的status字段修改为NORMAL；如果表不是这样的规则代表数据正常可用，请子类重写该方法
	 * @param pks
	 * @return 将数据的状态更新为正常可用
	 */
	public boolean softRecover(PK... pks){
		if(pks == null || pks.length <= 0) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "softRecover的参数不能为空");
		}
		
		getBaseDao().updateStatus(null, "status", CommonStatus.NORMAL.name(), pks);
		
		return true;
	}

	public List<T> findAll() {

		List<T> tList = getBaseDao().findAll(null, new PageBounds());

		return tList;
	}

	/**
	 * 
	 * @param property 字段名
	 * @param direction 排序方案
	 * @return
	 */
	public List<T> findAll(String property, Direction direction) {
		PageBounds pageBounds = new PageBounds(new Order(property, direction, null));

		return getBaseDao().findAll(null, pageBounds);
	}
	
	/**
	 * 
	 * @param orderstr 格式：property1.desc,property2.asc。例：name.desc,age.asc
	 * @return 
	 */
	public List<T> findAll(String orderstr) {
		PageBounds pageBounds = new PageBounds(Order.formString(orderstr));
		
		return getBaseDao().findAll(null, pageBounds);
	}
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	public List<T> findByEntity(T t){
		PageBounds pageBounds = new PageBounds();
		if(StringUtils.isNotBlank(t.getOrdseg())){
			List<Order> orders = Order.formString(t.getOrdseg());
			pageBounds.setOrders(orders);
		}
		
		return getBaseDao().findAll(t, pageBounds);
	}
	
	/**
	 * 
	 * @param t
	 * @param property 数据库字段名
	 * @param direction 排序方案
	 * @return
	 */
	public List<T> findByEntity(T t, String property, Direction direction){
		if(StringUtils.isBlank(property)){
			if(StringUtils.isBlank(t.getOrdseg())){
				return findByEntity(t);
			}
			
			return findByEntity(t, t.getOrdseg());
		} 
		
		t.setOrdseg(property + "." + direction);
		
		PageBounds pageBounds = new PageBounds(new Order(property, direction, null));
		
		return getBaseDao().findAll(t, pageBounds);
	}
	
	/**
	 * 
	 * @param t 
	 * @param orderstr 格式：property1.desc,property2.asc。例：name.desc,age.asc
	 * @return 
	 */
	public List<T> findByEntity(T t, String orderstr){
		if(StringUtils.isBlank(orderstr)){
			orderstr = t.getOrdseg();
		} else {
			t.setOrdseg(orderstr);
		}
        PageBounds pageBounds = new PageBounds(Order.formString(orderstr));
		
		return getBaseDao().findAll(t, pageBounds);
	}
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	public PageData<T> findByPage(T t){
		/*if(t == null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "方法findByPage的参数不能为空");
		}*/
		
		PageBounds pageBounds = parsePageBounds(t);
		
		List<T> tlist = getBaseDao().findAll(t, pageBounds);
		
		return new PageData<T>((PageList<T>)tlist);
	}
	
	/**
	 * 查询前N条数据
	 * @param t
	 * @param topNum
	 * @return
	 */
	public List<T> findTopItems(T t, int topNum){
		PageBounds pageBounds = new PageBounds(1, topNum);
		
		if(StringUtils.isNotBlank(t.getOrdseg())){
			List<Order> orders = Order.formString(t.getOrdseg());
			pageBounds.setOrders(orders);
		}
		List<T> tlist = getBaseDao().findAll(t, pageBounds);
		
		return tlist == null ? new ArrayList<T>() : new ArrayList<>(tlist);
	}
	
	/**
	 * 该方法将忽略t中的分页信息
	 * @param t
	 * @param currPage
	 * @param maxRow
	 * @return
	 */
	public PageData<T> findByPage(T t, int currPage, int maxRow){
		return findByPage(t, currPage, maxRow, null);
	}
	
	/**
	 * 该方法将忽略t中的分页信息
	 * @param t
	 * @param currPage
	 * @param maxRow
	 * @param orderstr  格式：property1.desc,property2.asc。例：name.desc,age.asc
	 * @return
	 */
	public PageData<T> findByPage(T t, int currPage, int maxRow, String orderstr){
		PageBounds pageBounds = new PageBounds(currPage, maxRow);
		if(StringUtils.isBlank(orderstr)){
			orderstr = t.getOrdseg();
		} else {
			t.setOrdseg(orderstr);
		}
		if(StringUtils.isNotBlank(orderstr)){
			List<Order> orders = Order.formString(orderstr);
			pageBounds.setOrders(orders);
		}
		List<T> tlist = getBaseDao().findAll(t, pageBounds);
		return new PageData<T>((PageList<T>)tlist);
	}
	
	protected PageBounds parsePageBounds(T t){
		PageBounds pageBounds = t == null ? null : t.getPageInfo();
		if(pageBounds == null){
			pageBounds = new PageBounds(1, BaseConfig.getInt("list.page.maxsize", 30));
		}else if(pageBounds.getLimit() == RowBounds.NO_ROW_LIMIT){
			pageBounds.setContainsTotalCount(true);
			pageBounds.setLimit(BaseConfig.getInt("list.page.maxsize", 30));
//			pageBounds.setLimit(Integer.MAX_VALUE - 100);
		} else if(pageBounds.getLimit() > 0){
			pageBounds.setContainsTotalCount(true);
		} else if(pageBounds.getLimit() == GlobalConstant.PAGE_NO_LIMIT){
			pageBounds.setContainsTotalCount(true);
			pageBounds.setLimit(Integer.MAX_VALUE - 100);
		}
		
		if(t != null && StringUtils.isNotBlank(t.getOrdseg())){
			List<Order> orders = Order.formString(t.getOrdseg());
			pageBounds.setOrders(orders);
		}
		
		return pageBounds;
	}
	

	public T findOne(T t) {

        PageBounds pageBounds = new PageBounds(1);
		
		List<T> tlist = getBaseDao().findAll(t, pageBounds);
		
		return CollectionUtils.isEmpty(tlist) ? null : tlist.get(0);
	}

	public T findOne(T t, String property, Direction direction) {
		PageBounds pageBounds = new PageBounds(new Order(property, direction, null));
		pageBounds.setLimit(1);

        List<T> tlist = getBaseDao().findAll(t, pageBounds);
		
		return CollectionUtils.isEmpty(tlist) ? null : tlist.get(0);
	}

	/**
	 * \r 55 \r 56
	 * 
	 * @param index
	 *            \r 57
	 * @return 返回泛型的类型\r 58
	 */
	@SuppressWarnings("all")
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

}
