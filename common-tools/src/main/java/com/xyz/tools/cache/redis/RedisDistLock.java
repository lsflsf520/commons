package com.xyz.tools.cache.redis;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.xyz.tools.cache.constant.DefaultJedisKeyNS;
import com.xyz.tools.common.utils.LogUtils;


public class RedisDistLock {
	
	private final static ThreadLocal<Map<String, String>> SUCC_LOCK_FLAG = new ThreadLocal<>();
	
	/**
	 * 调用本方法成功获取锁，待业务操作执行完之后一定要记得在finally块中调用 release(String resId)释放该资源锁，否则将会导致该资源锁上的其它线程一致等待直到锁失效（即3秒后缓存失效）
	 * 
	 * @param namespace 资源id所属的命名空间，可以为数据表名、数据类型名等，要确保命名空间的唯一性
	 * @param resId 资源ID
	 * @return 返回true，则代表获取到了锁； 返回false，则代表获取锁失败
	 */
	public static boolean trylock(String namespace, Serializable resId){
		try{
			boolean result = ShardJedisTool.setIfAbsent(DefaultJedisKeyNS.distlock, namespace + "_" + resId, 1);
			
			LogUtils.debug("get dist lock namespace:%s,resId:%s,result:%s", namespace, resId, result);
			if(result) {
				Map<String, String> succFlag = new HashMap<String, String>();
				succFlag.put(namespace + "|" + resId, "1");
				SUCC_LOCK_FLAG.set(succFlag);
			}
			return result;
		} catch (Exception e) {
			LogUtils.warn("get dist lock for namespace %s and resId %s error", e, namespace, resId);
		}
		
		return false;
	}
	
	/**
	 * 该方法最好与trylock成对出现，否则可能会有性能问题
	 * @param resId
	 */
	public static void release(String namespace, Serializable resId){
		Map<String, String> succFlag = SUCC_LOCK_FLAG.get();
		if(!CollectionUtils.isEmpty(succFlag) && StringUtils.isNotBlank(succFlag.get(namespace + "|" + resId))) {
			try{
				ShardJedisTool.del(DefaultJedisKeyNS.distlock, namespace + "_" + resId);
				LogUtils.debug("release dist lock namespace:%s,resId:%s", namespace, resId);
			} catch (Exception e) {
				LogUtils.warn("release dist lock for namespace %s and resId %s error", e, namespace, resId);
			}finally {
				SUCC_LOCK_FLAG.remove();
			}
		}
		
	}
	
	@Deprecated
	public static boolean trylock(String resId){
		
		return trylock("", resId);
	}
	
	@Deprecated
	public static void release(String resId){
		release("", resId);
	}

}
