package com.xyz.tools.common.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Administrator
 *
 */
public interface TreeBean<PK extends Serializable, E extends TreeBean<PK, E>> extends PKBean<PK>{

	/**
	 * 
	 * @return 返回当前对象的父ID
	 */
	PK getParentId();
	
	/**
	 * 获取排序码
	 * @return
	 */
	Integer getPriority(); 
	
	/**
	 * 
	 * @return 如果当前对象是树的根节点，则返回true；否则返回false
	 */
	boolean isRoot();
	
	/**
	 * 
	 * @param child 添加子节点
	 */
	void addChild(E child);
	
	/**
	 * 
	 */
	void removeChild(PK k);
	
	/**
	 * 清空子节点
	 */
	void clearChild();
	
	/**
	 * 
	 * @return 如果有子项，则返回true；否则返回false
	 */
	boolean hasChild();
	
	/**
	 * 
	 * @return 返回子项集合的一个迭代句柄
	 */
	List<E> getChildren();
	
	/**
	 * 
	 * @return
	 */
//	TreeBean<K> copyWithoutChild();
	
	/**
	 * 
	 * @return 返回当前节点在整棵树中的深度
	 */
//	int getLevel();
	
}
