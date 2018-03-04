package com.xyz.tools.cache.constant;


public interface JedisKeyNS extends CacheNameSpace {

	public static final int DEFAULT_TIME_WAIT = 2000; // 默认jedis操作超时时间，2秒
	public static final int RETRY_TIMES = 3;// 读写失败的重试次数
	public static final long SLEEP_TIME = 200;// 读写失败时重试时间间隔 单位 毫秒
	public static final String RDWNM_CODE = "RDWNM-500";

	public JedisKeyType getKeyType();

}
