package com.ujigu.secure.cache.constant;

public enum DefaultJedisKeyNS implements JedisKeyNS{
	
	testKey(55, JedisKeyType.STRING, "测试用的key"),
	session(24*60*60, JedisKeyType.HASH, "存储前端用户session信息的key"), //前端应用的session
	nsession(24*60*60, JedisKeyType.STRING, "新版的用户session，直接将对象转成json存储"),//新版
	uid2t(24*60*60, JedisKeyType.HASH, "存储用户id与登录token之间的关系，可能有PC的TOKEN、APP的token"),
//	bgsession(24*60*60, JedisKeyType.HASH, "存储admin端用户session信息的key"), //admin端应用的session
	mb_vc(5*60, JedisKeyType.HASH, "手机验证码命名空间"),
	ei(24*60*60, JedisKeyType.STRING, "错误输入次数统计"),
	img_vc(10*60, JedisKeyType.STRING, "图片验证码命名空间"),
	lk(24*60*60, "后台菜单权限，以JSON字符串格式存储"),
	site_info(24*60*60, JedisKeyType.HASH, "存储网站信息"),
	company(24*60*60, JedisKeyType.STRING, "存储公司信息"),
//	wx_number(24*60*60, JedisKeyType.STRING, "微信公众号信息"),
	nrsubmit(24*60*60, JedisKeyType.STRING, "防表单重复提交和防多用户编辑同一数据导致脏数据情况"),
	common_header(60*60, JedisKeyType.STRING, "存储网站标题头信息"),
	company_index(10*60, JedisKeyType.HASH, "存储company首页信息"),
	product_data(24*60*60, JedisKeyType.STRING, "线上产品信息"),
	product_class(24*60*60,JedisKeyType.STRING, "产品分类单个对象"),
	productclass_tree(24*60*60,JedisKeyType.STRING, "产品分类树形结构"),
	distlock(3, JedisKeyType.STRING, "模拟分布式资源锁，3秒内某个key获取到的数值为0，则获得锁立马执行；否则需要等待锁释放或者该key超过失效时间(3秒)，该锁不能用于耗时较长的操作"),
	msgvalve_day(24*60*60, JedisKeyType.STRING, "消息阀门，每天每个邮件地址发送的最大消息量"),
	msgvalve_m(60, JedisKeyType.STRING, "消息阀门，每分钟发送的最大消息量"),
	pingan_token(30*24*60*60,JedisKeyType.STRING,"平安access_token"),
	month_com_site(24*60*60, JedisKeyType.HASH, "存储佣金月结网站信息"),
	day_com_site(24*60*60, JedisKeyType.HASH, "存储佣金日结网站信息"),
	pro_recom_site(24*60*60, JedisKeyType.HASH, "存储代理人模式网站信息"),
	agent_recom_site(24*60*60, JedisKeyType.HASH, "存储推荐人模式网站信息"),
	anlian_password(24*60*60, JedisKeyType.STRING, "安联的密码"),
	product_job(24*60*60, JedisKeyType.HASH, "职业"),
	budget_rule(24*60*60, JedisKeyType.STRING, "产品规则"),
	job(24*60*60, JedisKeyType.HASH, "新职业"),
	order_user_comm(24*60*60, JedisKeyType.STRING, "订单非给用户的佣金和"),
	
	csai_cms(30*60, JedisKeyType.HASH, "用于存储csai的cms内容数据"),
	user_order_num(24*60*60, JedisKeyType.STRING, "用户订单数"),
	plan_product(24*60*60, JedisKeyType.STRING, "计划书产品信息"),
	month_wallet(24*60*60, JedisKeyType.STRING, "收入走势"),
	car_area_tree(24*60*60, JedisKeyType.STRING, "车险地区树"),
	app_agent(24*60*60, JedisKeyType.STRING, "开通app的代理人"),
	registration_id(24*60*60, JedisKeyType.STRING,"消息推送的别名"),
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
	
	/**
	 * 这个构造函数定义的key仅用于BaseServiceImpl的getCacheNameSpace(); 其key的类型为JedisKeyType.HASH
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
