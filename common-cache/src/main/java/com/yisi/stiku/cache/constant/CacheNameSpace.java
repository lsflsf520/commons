package com.yisi.stiku.cache.constant;

public interface CacheNameSpace {
	
	public final static String KEY_SPLITER = "_";

	/**
	 * 
	 * @return
	 */
	String getNameSpace();
	
	/**
	 * 
	 * @return 返回过期时间，以秒为单位
	 */
	int getExpire();
	
	/**
	 * 
	 * @return  在增删改该命名空间内的任一数据之后，是否需要清空命名空间内的全部缓存。返回true，则全部清空，否则只更新对应的数据缓存
	 */
	boolean needRemoveAllCacheAfterModify();
	
}
