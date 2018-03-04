package com.xyz.tools.common.bean;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.util.CollectionUtils;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.db.bean.BaseEntity;

public abstract class AbstractTreeBean<PK extends Serializable, E extends TreeBean<PK, E>> extends BaseEntity<PK> implements TreeBean<PK, E> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected List<E> children = new CopyOnWriteArrayList<E>();

	@Override
	public boolean isRoot() {
		return this.getParentId() == null || 
				this.getParentId().equals(this.getPK()) ||
				( (this.getParentId() instanceof Integer || this.getParentId() instanceof Long) && Long.valueOf(this.getParentId().toString()) <= 0l );
	}

	@Override
	public void addChild(E child) {
		List<E> childs = getChildren();
		if(childs == null){
			throw new BaseRuntimeException("ILLEGAL_STATE", "当前没有可用的子对象集合，不支持此操作");
		}
		
		childs.add(child);
	}

	@Override
	public void removeChild(PK k) {
		List<E> childs = getChildren();
		if(childs == null){
			throw new BaseRuntimeException("ILLEGAL_STATE", "当前没有可用的子对象集合，不支持此操作");
		}
		E temp = null;
	    for(E child : childs){
	    	if(child.getPK().equals(k)){
	    		temp = child;
	    		break;
	    	}
	    }
		
	    if(temp != null){
	    	childs.remove(temp);
	    }
		
	}
	
	@Override
	public void clearChild() {
		List<E> childs = getChildren();
		if(childs != null){
			childs.clear();
		}
		
	}

	@Override
	public boolean hasChild() {
		return !CollectionUtils.isEmpty(getChildren());
	}

	@Override
	public Integer getPriority() {
		return 0;
	}
	
	
	@Override
	public List<E> getChildren() {
		return children;
	}


}
