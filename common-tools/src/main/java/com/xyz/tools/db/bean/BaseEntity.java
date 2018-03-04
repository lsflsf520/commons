package com.xyz.tools.db.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.xyz.tools.common.bean.PKBean;
import com.xyz.tools.common.constant.GlobalConstant;

public abstract class BaseEntity<PK extends Serializable> implements Serializable, PKBean<PK>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 接受分页参数
	 */
//	@JsonIgnore
	protected PageBounds pageInfo; //此处修饰为 transient 主要是为了不让Gson序列化此值
	
	/**
	 * 接收排序参数。 格式：columnName1.desc,columnName2.asc。例：name.desc,age.asc
	 */
//	@JsonIgnore
	protected String ordseg; 
	
	/**
	 * 接收除本entity固有属性外的其它查询参数。在页面的form中，表单元素的name必须要写成如下形式才能被正确接收（即用中括号括起来的形式，不能用点语法）
	 *   <input type='text' name='queryParam["keyword"]' >
	 *   <input type='text' name='queryParam["startDate"]' >
	 *   <input type='text' name='queryParam["startDate"]' >
	 */
//	@JsonIgnore
	protected Map<String, Object> queryParam;
	

	/**
	 * 
	 */
	public void setPK(PK pk){
		
	}

	@JsonIgnore
	public PageBounds getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(PageBounds pageInfo) {
		this.pageInfo = pageInfo;
	}
	
	public void setPage(int page){
		if(pageInfo == null){
			pageInfo = new PageBounds(page, GlobalConstant.PAGE_NO_LIMIT);
		} else {
			pageInfo.setPage(page);
		}
	}
	
	public void setLimit(int limit){
		if(pageInfo == null){
			pageInfo = new PageBounds(0, limit);
		} else {
			pageInfo.setLimit(limit);
		}
	}

	@JsonIgnore
	public Map<String, Object> getQueryParam() {
		return queryParam;
	}

	public void setQueryParam(Map<String, Object> queryParam) {
		this.queryParam = queryParam;
	}
	
	/**
	 * 添加一个查询参数
	 * @param key
	 * @param val
	 */
	public void addQueryParam(String key, Object val){
		if(this.queryParam == null){
			this.queryParam = new HashMap<>();
		}
		
		this.queryParam.put(key, val);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getQueryParam(String key){
		if(this.queryParam == null){
			return null;
		}
		
		return (T)this.queryParam.get(key);
	}
	
	public void removeParam(String key){
		if(this.queryParam != null){
			this.queryParam.remove(key);
		}
	}

	@JsonIgnore
	public String getOrdseg() {
		return ordseg;
	}

	public void setOrdseg(String ordseg) {
		this.ordseg = ordseg;
	}
	
}
