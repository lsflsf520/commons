package com.yisi.stiku.cache.constant;

/**
 * Created by mk on 2016/1/29.
 */
public abstract class AbstractJedisKeyNs implements JedisKeyNS {

	protected String nameSpace;
	protected int expireTime;
	protected JedisKeyType keyType;
	protected String desc;
	protected boolean needRemoveAllCacheAfterModify;

	public abstract AbstractJedisKeyNs createInstance(String nameSpace, int expireTime, JedisKeyType keyType, String desc,
			boolean needRemoveAllCacheAfterModify);

	@Override
	public JedisKeyType getKeyType() {

		return this.keyType;
	}

	@Override
	public String getNameSpace() {

		return this.nameSpace;
	}

	@Override
	public int getExpire() {

		return this.expireTime;
	}

	@Override
	public boolean needRemoveAllCacheAfterModify() {

		return this.needRemoveAllCacheAfterModify;
	}

	public String getDesc() {

		return this.desc;
	}
}
