package com.yisi.stiku.db.multi;

import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * 主从数据源路由策略
 * @author shangfeng
 *
 */
public class MSDSKeyHolder {
	
	public final static String FORCE_MASTER = "forceMaster";
	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

	/**
	 * 
	 * @param ms
	 * @param parameter
	 */
	public static void preUpdate(MappedStatement ms, Object parameter){
		contextHolder.set(FORCE_MASTER);
	}
	
	/**
	 * 
	 * @param ms
	 * @param boundSql
	 */
	public static void preQuery(MappedStatement ms, BoundSql boundSql){
		Object paramObj = boundSql.getParameterObject();
		Map paramMap = null;
		if(paramObj instanceof Map && (paramMap = (Map)paramObj).containsKey(FORCE_MASTER)){
			Object forceMaster = paramMap.get(FORCE_MASTER);
			if(forceMaster != null && "true".equalsIgnoreCase(forceMaster.toString())){
				contextHolder.set(FORCE_MASTER);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isForceMaster(){
		return FORCE_MASTER.equals(contextHolder.get());
	}
	
	public static void clearKey(){
		contextHolder.remove();
	}
	
}
