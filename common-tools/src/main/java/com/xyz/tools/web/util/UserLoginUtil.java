package com.xyz.tools.web.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.xyz.tools.cache.constant.DefaultJedisKeyNS;
import com.xyz.tools.cache.redis.ShardJedisTool;
import com.xyz.tools.common.bean.IUser;
import com.xyz.tools.common.constant.GlobalConstant;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.EncryptTools;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RandomUtil;
import com.xyz.tools.common.utils.RegexUtil;
import com.xyz.tools.common.utils.ThreadUtil;

/**
 * 跟登录相关的session操作工具，包含存储session信息和获取session信息等
 * 
 * @author shangfeng
 *
 */
public class UserLoginUtil {

	private final static Logger LOG = LoggerFactory.getLogger(UserLoginUtil.class);

//	private final static String CLIENT_TOKEN_SUFFIX = "-tk";
//	private final static String CLIENT_EQUIP_SUFFIX = "-tp";
	
	public final static int WEEK_SECONDS = 7*24*3600;

	/**
	 * 
	 * @param sessionMap
	 * @return session存储成功之后，返回该session的token值，用于写回到cookie中
	 */
	public static String storeSession(IUser currUser, HttpServletResponse response, boolean remindMe) {

		if (currUser == null || currUser.getUid() <= 0
				) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "系统异常，请重试或联系管理员！", "session信息不合法");
		}
		String clientIp = ThreadUtil.getSrcIP();
		String equipType = ThreadUtil.getEquipType();
		long currTime = System.currentTimeMillis();
		int randCode = RandomUtil.rand(100000);

		String token = EncryptTools.encryptByMD5(clientIp + equipType + currUser.getUid() + currTime + randCode);

		boolean success = ShardJedisTool.hset(DefaultJedisKeyNS.session, token, "userinfo", JsonUtil.create().toJson(currUser));
		if (!success) {
			throw new BaseRuntimeException("SESSION_STORE_ERROR", "系统异常，请重试或联系管理员！", "session存储失败");
		}
		
		if(remindMe){
			ShardJedisTool.expire(DefaultJedisKeyNS.session, token, WEEK_SECONDS);
			ShardJedisTool.expire(DefaultJedisKeyNS.uid2t, getUidKey(currUser.getUid() + ""), WEEK_SECONDS);
		}

//		Map<String, Object> uidTkMap = new HashMap<String, Object>();
		String clientType = getClientType();
//		uidTkMap.put(clientType + CLIENT_TOKEN_SUFFIX, token);
//		uidTkMap.put(clientType + CLIENT_EQUIP_SUFFIX, equipType);
		
		saveUid2Token(currUser.getUid() + "", clientType, token);
		
		WebUtils.setHttpOnlyCookie(response, ThreadUtil.TOKEN, token,
				remindMe ? WEEK_SECONDS : -1); // cookie默认24小时后失效

		return token;
	}
	
	private static void saveUid2Token(String userId, String clientType, String token){
		ShardJedisTool.hset(DefaultJedisKeyNS.uid2t, getUidKey(userId), clientType, token);
	}
	
	/**
	 * 管理员往前端登录用户的session中添加或修改字段值
	 * @param userId
	 * @param field
	 * @param value
	 */
	public static void add2SessionByUid(int userId, String field, Serializable value){
		Map<String, String> vals = ShardJedisTool.hgetAll(DefaultJedisKeyNS.uid2t, "f" + userId);
		if(!CollectionUtils.isEmpty(vals)){
			for(String token : vals.values()){
				if(StringUtils.isNotBlank(token)){
					add2Session(token, field, value);
				}
			}
		}
	}
	
	private static String getUidKey(String userId){
		String prefix = "f";
		if(GlobalConstant.IS_MGR){
			prefix = "b";
		}
		
		return prefix + userId;
	}
	
	/**
	 * 
	 * @param response
	 */
	public static void removeSession(HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(ThreadUtil.getToken())) {
			try{
				long userId = ThreadUtil.getUid();
				String clientType = getClientType();
				ShardJedisTool.hdel(DefaultJedisKeyNS.uid2t, getUidKey(userId + ""), clientType);
				ShardJedisTool.del(DefaultJedisKeyNS.session, ThreadUtil.getToken()); // 先删除缓存中的session信息
			} catch(BaseRuntimeException e){
				LogUtils.error("already logout", e, "");
			}
		}
		WebUtils.deleteAllCookies(request, response);
	}
	
	public static <T extends IUser> T getSessionInfo() {
		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("NOT_LOGON", "用户尚未登录");
		}

		String userInfo = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, "userinfo");
		if(StringUtils.isNotBlank(userInfo)) {
			T t = JsonUtil.create().fromJson(userInfo, new TypeToken<T>() {}.getType());
			
			ThreadUtil.setCurrUser(t);
			
			return t;
		}
		throw new BaseRuntimeException("NOT_LOGON", "用户尚未登录或会话已过期，请重新登录");
	}

	/**
	 * 
	 * @return 根据当前用户登录的客户端类型，返回存储被kickoff原因的key
	 */
	public static String getKOMsgKey() {

		String ctype = getClientType();
		return getKOMsgKey(ctype);
	}

	/**
	 * 
	 * @param ctype
	 * @return 根据指定的ctype字符串，组装成存储被kickoff原因的key
	 */
	private static String getKOMsgKey(String ctype) {

		return ctype + "_ko";
	}

	/**
	 * 
	 * @return 如果是app登录则返回“app”字符串，如果是网页登录，则返回“web”字符串
	 */
	public static String getClientType() {

		if (ThreadUtil.isAppReq()) {
			return "app";
		} else if(ThreadUtil.isWxClient()){
			return "wx";
		} else if(ThreadUtil.isMobileClient()){
			return "mob";
		}

		return "web";
	}
	
	/**
	 * 往当前登录用户的session中添加或修改字段值
	 * @param key
	 * @param data
	 */
	public static void add2Session(String key, Serializable data){
		
		String token = ThreadUtil.getToken();
		
		add2Session(token, key, data);
	}
	
	private static void add2Session(String token, String key, Serializable data){
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}
		if(data == null || StringUtils.isBlank(key)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "key或者data都不能为空");
		}
		String val = (data instanceof String) ? data.toString() : new Gson().toJson(data);
		boolean success = ShardJedisTool.hset(DefaultJedisKeyNS.session, token, key, val);
		if (!success) {
			throw new BaseRuntimeException("SESSION_STORE_ERROR", "session存储失败");
		}
		ThreadUtil.put(key, val);
	}

	public static void updateSessionInfo(Map<String, String> sessionInfoMap) {
		if(sessionInfoMap == null || sessionInfoMap.isEmpty()){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "参数不能为空");
		}

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}
		Map<String, Object> infoMap = new HashMap<>();
		for(String key : sessionInfoMap.keySet()){
			infoMap.put(key, sessionInfoMap.get(key));
		}
		boolean success = ShardJedisTool.hmset(DefaultJedisKeyNS.session, token, infoMap);
		if (!success) {
			throw new BaseRuntimeException("SESSION_STORE_ERROR", "session存储失败");
		}
	}

	/**
	 * 顺延session的失效时间
	 */
	public static void continueSessionTTL() {

		try {
			ShardJedisTool.expire(DefaultJedisKeyNS.session, ThreadUtil.getToken(), DefaultJedisKeyNS.session.getExpire());
			long userId = ThreadUtil.getUid();
			ShardJedisTool.expire(DefaultJedisKeyNS.uid2t, getUidKey(userId + ""), DefaultJedisKeyNS.session.getExpire());
		} catch (Exception e) {
			LOG.warn("continue session ttl failure.");
		}
	}

	/**
	 * 
	 * @return 如果用户已经登录，则返回true，否则返回false
	 */
	public static boolean hasLogon() {

		try {
			String token = ThreadUtil.getToken();
			return StringUtils.isNotBlank(token) && ThreadUtil.getUid() > 0;
//					&& StringUtils.isBlank(ShardJedisTool.hget(DefaultJedisKeyNS.session, token, getKOMsgKey()));
		} catch (BaseRuntimeException e) {
			return false;
		}
	}

	
	private static String getStrFromSession(String field, boolean excptWhenNotLogon, boolean excptWhenNotExist){
		Serializable val = getFromSession(field, excptWhenNotLogon, excptWhenNotExist);
		
		if(val != null){
			ThreadUtil.putIfAbsent(field, val.toString());
		}
		
		return (String)val;
	}
	
	private static Integer getIntFromSession(String field, boolean excptWhenNotLogon, boolean excptWhenNotExist){
        Serializable val = getFromSession(field, excptWhenNotLogon, excptWhenNotExist);
		
		if(val != null && RegexUtil.isInt(val.toString())){
			Integer valI =  Integer.valueOf(val.toString());
			ThreadUtil.putIfAbsent(field, valI);
			return valI;
		}
		
		return null;
	}
	
    private static Serializable getFromSession(String field, boolean excptWhenNotLogon, boolean excptWhenNotExist){
    	Serializable val = ThreadUtil.get(field);
		if (val != null) {
			return val;
		}
		
		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			if(excptWhenNotLogon){
				throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
			}
			return null;
		}

		val = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, field);
		if(val == null && excptWhenNotExist){
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录或session已失效");
		}
		return val;
	}
	
	
	public static String getSessionValue(String key){
		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}
		
		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, key);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getToken(HttpServletRequest request) {

		return WebUtils.getCookieValue(request, ThreadUtil.TOKEN);
	}

	/**
	 * 以sessionId为key，验证码为值存储到redis中，在用户登录前(即登录页、注册页)和初始密码重置页面用得着
	 * 
	 * @param request
	 */
	public static void saveTmpSessionInfo(HttpServletRequest request, String code, String mobileOrEmail) {

		String key = request.getSession().getId();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", code);
		map.put("mobileOrEmail", mobileOrEmail);

		ShardJedisTool.hmset(DefaultJedisKeyNS.mb_vc,
				key, map);
	}

	/**
	 * 
	 * @param request
	 * @param code
	 * @param needMobileOrEmailEqual 是否需要校验mobileOrEmail一致
	 * @return 判断验证码是否正确，如果正确，则返回true；否则返回false
	 */
	private static boolean validateCode(HttpServletRequest request, String code, String mobileOrEmail, boolean needMobileOrEmailEqual) {

		String key = request.getSession().getId();
		
		Map<String, String> valMap = ShardJedisTool.hgetAll(DefaultJedisKeyNS.mb_vc, key);
		if(valMap == null || valMap.isEmpty()){
			return false;
		}

		return StringUtils.isNotBlank(code) && code.equalsIgnoreCase(valMap.get("code")) 
				&& (!needMobileOrEmailEqual || (StringUtils.isNotBlank(mobileOrEmail) && mobileOrEmail.equalsIgnoreCase(valMap.get("mobileOrEmail"))) );
	}
	
	public static boolean validateCode(HttpServletRequest request, String code, String mobileOrEmail){
		return validateCode(request, code, mobileOrEmail, true);
	}
	
	public static boolean validateCode(HttpServletRequest request, String code){
		return validateCode(request, code, null, false);
	}

	/**
	 * 
	 * @param request
	 * @return 返回存储验证码时，一起存入的邮箱或者手机号
	 */
	public static String getMobileOrEmail(HttpServletRequest request) {

		String key = request.getSession().getId();

		return ShardJedisTool.hget(DefaultJedisKeyNS.mb_vc,
				key, "mobileOrEmail");
	}
	
	/**
	 * 
	 * @param key 存储图片验证码的id
	 * @param code 图片验证码的实际值
	 */
	public static void saveImgCode(String key, String code){
		ShardJedisTool.set(DefaultJedisKeyNS.img_vc, key, code);
	}
	
	/**
	 * 校验图片验证码
	 * @param key
	 * @param code
	 * @return
	 */
	public static boolean verifyImgCode(String key, String code){
		if(StringUtils.isBlank(key) || StringUtils.isBlank(code)){
			return false;
		}
		String cacheCode = ShardJedisTool.get(DefaultJedisKeyNS.img_vc, key);
		LogUtils.debug("verify image code key:%s code:%s cacheCode:%s", key, code, cacheCode);
		return code.trim().equalsIgnoreCase(cacheCode);
	}
	/**
	 * 校验图片验证码 最新
	 * @param key
	 * @param code
	 * @return
	 */
	public static boolean verifyImgCode(HttpServletRequest request, String code){
		String key = request.getParameter("imgKey");
		if(StringUtils.isBlank(key)){
			key = request.getSession().getId();
		}

		return verifyImgCode(key, code);
	}
	
	/**
	 * 
	 * @param 位数  默认6
	 * @return 返回6位数字随机验证码
	 */
	public static String createVerificationCode(int verificationCodeLength) 
    {
        //    所有候选组成验证码的字符，可以用中文
        String[] verificationCodeArrary={"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String verificationCode = "";
        Random random = new Random();
        //此处是生成验证码的核心了，利用一定范围内的随机数做为验证码数组的下标，循环组成我们需要长度的验证码，做为页面输入验证、邮件、短信验证码验证都行
        for(int i=0;i<verificationCodeLength;i++){
        	verificationCode += verificationCodeArrary[random.nextInt(verificationCodeArrary.length)];
        	}
        return verificationCode;
    }
	
	
	
	//验证是否为手机号
	 public static boolean checkMobile(String mobile) {  
         
         if(mobile.matches("^1[3|4|5|7|8][0-9]\\d{4,8}$")){  
        	 return true;
         }
         return false;  
     }  
	 
	 /**
	     * 全角转半角
	     * @param input String.
	     * @return 半角字符串
	     */
	    public static String ToDBC(String input) {
	        
	    	if(input == null || "".equals(input)){
	    		return input;
	    	}
	             char c[] = input.toCharArray();
	             for (int i = 0; i < c.length; i++) {
	               if (c[i] == '\u3000') {
	                 c[i] = ' ';
	               } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
	                 c[i] = (char) (c[i] - 65248);

	               }
	             }
	        String returnString = new String(c);
	        
	             return returnString;
	    }
	    
}
