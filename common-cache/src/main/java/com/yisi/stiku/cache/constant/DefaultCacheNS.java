package com.yisi.stiku.cache.constant;

public enum DefaultCacheNS implements CacheNameSpace{

//	SYS_MENU(1800, true),
//	SYS_PRIV(1800, true),
//	SYS_ROLE(1800),
//	DATA_PRIV(1800), //数据权限
	BASE_AREA(24*60*60), //一天失效
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
