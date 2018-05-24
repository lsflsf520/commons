package com.xyz.tools.cache.constant;

public enum DefaultJedisKeyNS implements JedisKeyNS {

	session(24 * 60 * 60, JedisKeyType.HASH, "存储前端用户session信息的key"), // 前端应用的session
	
	nsession(24*60*60, JedisKeyType.STRING, "新版的用户session，直接将对象转成json存储"),//新版
	uid2t(24 * 60 * 60, JedisKeyType.HASH, "存储用户id与登录token之间的关系，可能有PC的TOKEN、APP的token"),
	// bgsession(24*60*60, JedisKeyType.HASH, "存储admin端用户session信息的key"),
	// //admin端应用的session
	mb_vc(5*60, JedisKeyType.HASH, "手机验证码命名空间"),
	ei(24*60*60, JedisKeyType.STRING, "错误输入次数统计"),
	img_vc(10*60, JedisKeyType.STRING, "图片验证码命名空间"),
	nrsubmit(24*60*60, JedisKeyType.STRING, "防表单重复提交和防多用户编辑同一数据导致脏数据情况"),
	distlock(3, JedisKeyType.STRING, "模拟分布式资源锁，3秒内某个key获取到的数值为0，则获得锁立马执行；否则需要等待锁释放或者该key超过失效时间(3秒)，该锁不能用于耗时较长的操作"),
//	lk(24 * 60 * 60, "后台菜单权限，以JSON字符串格式存储"),

	;

	private final int expireTime;
	private final JedisKeyType keyType;
	private final String desc;
	private final boolean needRemoveAllCacheAfterModify;
	
	private DefaultJedisKeyNS(int expireTime, JedisKeyType keyType, String desc){
		this.expireTime = expireTime;
		this.keyType = keyType;
		this.desc = desc;
		this.needRemoveAllCacheAfterModify = false;
	}

	@Override
	public String getNameSpace() {
		return this.name();
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
		return desc;
	}

	public JedisKeyType getKeyType() {
		return keyType;
	}

}
