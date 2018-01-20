package com.ujigu.secure.cache.redis;

import com.ujigu.secure.cache.constant.DefaultJedisKeyNS;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.RegexUtil;

public class RedisDistLock {
	
	/**
	 * 调用本方法成功获取锁，待业务操作执行完之后一定要记得在finally块中调用 release(String resId)释放该资源锁，否则将会导致该资源锁上的其它线程一致等待直到锁失效（即3秒后缓存失效）
	 * 
	 * @param resId 资源ID
	 * @return 为避免死锁，以下几种情况均为已获取到锁
	 * 1、获取到资源resId对应的信号值小于等于0
	 * 2、在获取资源值的过程中发生异常，或者在等待锁释放的过程中发生异常的次数超过3次
	 * 3、在等待锁释放的过程中，如果资源对应的锁不存在（即缓存失效）或者资源对应的信号值与刚进入本方法时为该资源值进行原子增操作前的值相等
	 */
	public static boolean trylock(String resId){
		try{
			long sigNum = ShardJedisTool.incr(DefaultJedisKeyNS.distlock, resId);
			if(sigNum <= 0l){
				return true;
			}
			
			int errorNum = 0;
			while(true){
				try {
					Thread.sleep(100);
					String currSigNumStr = ShardJedisTool.get(DefaultJedisKeyNS.distlock, resId);
					if(!RegexUtil.isInt(currSigNumStr) || Long.valueOf(currSigNumStr).equals(sigNum)){
						return true;
					}
				} catch (InterruptedException e) {
					LogUtils.warn("InterruptedException occur while sleep to get dist lock for resource %s error", resId);
					++errorNum;
				} catch (Exception e) {
					LogUtils.warn("get dist lock for resource %s error", e, resId);
					++errorNum;
				}
				//为避免出现死锁，等待锁释放的过程中发生3次以上的异常，则返回true，即代表锁获取成功
				if(errorNum >= 3){
					return true;
				}
			}
		} catch (Exception e) {
			LogUtils.warn("get dist lock for resource %s error", e, resId);
		}
		
		return true;
	}
	
	/**
	 * 该方法最好与trylock成对出现，否则可能会有性能问题
	 * @param resId
	 */
	public static void release(String resId){
		try{
			long sigNum = ShardJedisTool.decr(DefaultJedisKeyNS.distlock, resId);
            if(sigNum <= 0){
            	ShardJedisTool.del(DefaultJedisKeyNS.distlock, resId);
            }
		} catch (Exception e) {
			LogUtils.warn("release dist lock for resource %s error", e, resId);
		}
		
	}

}
