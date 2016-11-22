package com.yisi.stiku.cache.constant;

public enum DefaultJedisKeyNS implements JedisKeyNS {

	testKey(55, JedisKeyType.STRING, "测试用的key"),
	session(60 * 60, JedisKeyType.HASH, "存储用户session信息的key"), // session默认闲置1小时后过期
	uid_token(4 * 60 * 60, JedisKeyType.HASH, "存储用户id和session的token之间的对应关系"),
	vc(5 * 60, JedisKeyType.HASH, "验证码命名空间"),
	ei(24 * 60 * 60, JedisKeyType.STRING, "错误输入次数统计"),

	index(60 * 60, JedisKeyType.HASH, "学生首页跳转的缓存"),

	flowcrtl(10, JedisKeyType.STRING, "流控信息"),

	pc(60 * 60, JedisKeyType.STRING, "problem_content的基础缓存"),

	bookSort(0, JedisKeyType.STRING, "教材顺序"),
	lk(24*60*60, "后台菜单权限，以JSON字符串格式存储"),
	dp(1800,"数据权限缓存"),
	ba(0, "省市县等地理信息"),
	asyncJobAttr(90 * 60, JedisKeyType.HASH,"后台异步任务框架任务属性信息"),
	asyncLbTaskDetail(90 * 60, JedisKeyType.LIST,"后台异步任务框架任务处理详情"),
	asyncLbTasks(30 * 60, JedisKeyType.STRING,"后台异步任务框架task的存放空间"),
	
	makeMagazineRetry(12 *60*60, JedisKeyType.STRING,"生成线下作业重试"),
	makeMagazineInit(10*60, JedisKeyType.STRING,"系统启动时检查线下未完成的线下作业"),
	
	examFeedbackClass(30 * 60, JedisKeyType.STRING,"试卷反馈班级数据"),
	;

	private final int expireTime;
	private final JedisKeyType keyType;
	private final String desc;
	private final boolean needRemoveAllCacheAfterModify;

	private DefaultJedisKeyNS(int expireTime, JedisKeyType keyType, String desc) {

		this.expireTime = expireTime;
		this.keyType = keyType;
		this.desc = desc;
		this.needRemoveAllCacheAfterModify = false;
	}

	/**
	 * 这个构造函数定义的key仅用于BaseServiceImpl的getCacheNameSpace();
	 * 其key的类型为JedisKeyType.HASH
	 * 
	 * @param expireTime
	 * @param desc
	 */
	private DefaultJedisKeyNS(int expireTime, String desc) {

		this.expireTime = expireTime;
		this.keyType = JedisKeyType.HASH;
		this.desc = desc;
		this.needRemoveAllCacheAfterModify = true;
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

	@Override
	public JedisKeyType getKeyType() {

		return this.keyType;
	}

	public String getDesc() {

		return desc;
	}

}
