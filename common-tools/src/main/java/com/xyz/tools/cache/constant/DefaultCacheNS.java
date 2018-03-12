package com.xyz.tools.cache.constant;

public enum DefaultCacheNS implements CacheNameSpace{

	SYS_FUNC(1800, true),
	SYS_DEPART(1800),
	SYS_ROLE(1800),
	SYS_WEBAPP(1800),
	WORKER_DEPART_ROLE(1800),
	WORKER_FUNC(1800),
	ROLE_FUNC(1800),
	DATA_PRIV(1800),
	
	BASE_DISTRICT(24*60*60), //地区数据
	BASE_INDUSTRY(24*60*60), //行业数据
	;
	
	private final int expireSeconds;
	private final boolean removeAllAfterModify;
	private DefaultCacheNS(int expireSeconds){
		this(expireSeconds, true);
	}
	
	private DefaultCacheNS(int expireSeconds, boolean removeAllAfterModify){
		this.expireSeconds = expireSeconds;
		this.removeAllAfterModify = removeAllAfterModify;
	}
	
	@Override
	public int getExpire() {
		return expireSeconds;
	}
	
	@Override
	public String getNameSpace() {
		return this.name();
	}

	@Override
	public boolean needRemoveAllCacheAfterModify() {
		return removeAllAfterModify;
	}
	
	
}
