package com.xyz.tools.web.util;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.google.common.reflect.TypeToken;
import com.xyz.tools.cache.constant.DefaultJedisKeyNS;
import com.xyz.tools.cache.redis.ShardJedisTool;
import com.xyz.tools.common.constant.ClientType;
import com.xyz.tools.common.constant.EquipType;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.DateUtil;
import com.xyz.tools.common.utils.EncryptTools;
import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RandomUtil;
import com.xyz.tools.common.utils.StringUtil;
import com.xyz.tools.common.utils.ThreadUtil;

public class LogonUtil {
	
	public final static int WEEK_SECONDS = 7*24*3600;
	public final static int MONTH_SECONDS = 30*24*3600;
	
	private final static String REMIND_ME_KEY = "_rme";
	private final static String REMIND_ME_VAL = "T";
	
	public static String storeSession(HttpServletRequest request, HttpServletResponse response, SessionUser suser, boolean remindMe){
		if(suser == null || suser.getUid() <= 0){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "系统异常，请重试或联系管理员！", "session信息不合法");
		}
		String clientIp = WebUtils.getClientIp(request);
		clientIp = StringUtils.isBlank(clientIp) ? IPUtil.getLocalIp() : clientIp;
		ClientType clientType = ThreadUtil.getClientType();
		long currTime = System.currentTimeMillis();
		int randCode = RandomUtil.rand(100000);
		String token = EncryptTools.encryptByMD5(clientIp + clientType + suser.getUid() + currTime + randCode);
		if(suser.getLastLoginTime() == null){
			suser.setLastLoginTime(DateUtil.getCurrentDateTimeStr());
		}
		/*if(suser.getLastLoginEquip() == null){
			suser.setLastLoginEquip(equipType);
		}*/
		if(suser.getLastLoginIP() == null){
			suser.setLastLoginIP(clientIp);
		}
		if(suser.getLastLoginClientType() == null){
			suser.setLastLoginClientType(clientType);
		}
		boolean success = ShardJedisTool.set(DefaultJedisKeyNS.nsession, token, JsonUtil.create().toJson(suser));
		if (!success) {
			throw new BaseRuntimeException("SESSION_STORE_ERROR", "系统异常，请重试或联系管理员！", "session存储失败");
		}
		
		saveUid2Token(suser.getUserClsId(), suser.getLastLoginClientType(), token);
		if(remindMe){
			ShardJedisTool.expire(DefaultJedisKeyNS.nsession, token, WEEK_SECONDS);
			ShardJedisTool.expire(DefaultJedisKeyNS.uid2t, suser.getUserClsId(), WEEK_SECONDS);
			
			WebUtils.setHttpOnlyCookie(response, REMIND_ME_KEY, REMIND_ME_VAL, WEEK_SECONDS);
		}
		
		WebUtils.setHttpOnlyCookie(response, ThreadUtil.TOKEN_KEY, token,
				remindMe ? WEEK_SECONDS : -1); // cookie默认24小时后失效
		
		return token;
	}
	
	/**
	 * 是否有记住我一周的cookie
	 * @param request
	 * @return
	 */
	public static boolean isRemindMe(HttpServletRequest request){
		String cookieVal = WebUtils.getCookieValue(request, REMIND_ME_KEY);
		
		return REMIND_ME_VAL.equals(cookieVal);
	}
	
	/**
	 * 
	 * @param userId
	 * @param realName
	 */
	public static void updateRealNameInFrontSession(int userId, String realName) {
		updateFrontSessionByUid(userId, "realName", realName);
	}
	
	/**
	 * 
	 * @param userId
	 * @param phone
	 */
	public static void updatePhoneInFrontSession(int userId, String phone) {
		updateFrontSessionByUid(userId, "phone", phone);
	}
	
	/**
	 * 
	 * @param userId
	 * @param headImg
	 */
	public static void updateHeadImgInFrontSession(int userId, String headImg) {
		updateFrontSessionByUid(userId, "headImg", headImg);
	}
	
	/**
	 * 管理员往前端登录用户的session中添加或修改字段值
	 * @param userId
	 * @param field
	 * @param value
	 */
	private static void updateFrontSessionByUid(int userId, String field, Serializable value){
		String userClsId = buildUserClsId(UserCls.ft, userId);
		Map<String, String> vals = ShardJedisTool.hgetAll(DefaultJedisKeyNS.uid2t, userClsId);
		if(!CollectionUtils.isEmpty(vals)){
			for(String token : vals.values()){
				if(StringUtils.isNotBlank(token)){
					updateSession(token, field, value);
				}
			}
		}
	}
	
	/**
	 * 往当前登录用户的session中添加或修改字段值
	 * @param key
	 * @param data
	public static void add2Session(String key, Serializable data){
		
		String token = ThreadUtil.getToken();
		
		add2Session(token, key, data);
	}  */
	
	private static void updateSession(String token, String key, Serializable data){
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}
		if(data == null || StringUtils.isBlank(key)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "key或者data都不能为空");
		}
		String val = (data instanceof String) ? data.toString() : JsonUtil.create().toJson(data);
		
		String jsonStr = ShardJedisTool.get(DefaultJedisKeyNS.nsession, token);
		if(StringUtils.isBlank(jsonStr)){
			throw new BaseRuntimeException("SESSION_EXPIRED", "会话已过期");
		}
		
		Map<String, String> sessionMap = JsonUtil.create().fromJson(jsonStr, new TypeToken<Map<String, String>>() {}.getType());
		sessionMap.put(key, val);
		
		boolean success = ShardJedisTool.set(DefaultJedisKeyNS.nsession, token, JsonUtil.create().toJson(sessionMap));
		if (!success) {
			throw new BaseRuntimeException("SESSION_STORE_ERROR", "session存储失败");
		}
	}
	
	public static SessionUser getSessionUser(){
		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("NOT_LOGON", "用户尚未登陆");
		}

		String jsonStr = ShardJedisTool.get(DefaultJedisKeyNS.nsession, token);
		if(StringUtils.isBlank(jsonStr)){
			throw new BaseRuntimeException("SESSION_EXPIRED", "会话已过期");
		}
		
		SessionUser currUser = JsonUtil.create().fromJson(jsonStr, SessionUser.class);
		if(currUser == null || currUser.getUid() <= 0){
			throw new BaseRuntimeException("ILLEGAL_STATE", "会话信息有误", "token:" + token);
		}
		return currUser;
	}
	
	public static void continueSessionTTL(){
		if(ThreadUtil.isAppReq()) {
			continueSessionTTL(MONTH_SECONDS);
		} else {
			continueSessionTTL(DefaultJedisKeyNS.nsession.getExpire());
		}
	}
	
	public static void continueRemindMeSessionTTL() {
		continueSessionTTL(WEEK_SECONDS);
	}
	
	public static void continueSessionTTL(int expireSeconds) {
		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("NOT_LOGON", "用户尚未登陆");
		}
		
		try {
			ShardJedisTool.expire(DefaultJedisKeyNS.nsession, token, expireSeconds);
			SessionUser suser = ThreadUtil.getCurrUser();
			if(suser != null){
				ShardJedisTool.expire(DefaultJedisKeyNS.uid2t, suser.getUserClsId(), expireSeconds);
			}
		} catch (Exception e) {
			LogUtils.warn("continue session ttl failure. token:%s", token);
		}
	}
	
	public static void removeSession(HttpServletRequest request, HttpServletResponse response){
		String token = ThreadUtil.getToken();
		if (StringUtils.isNotBlank(token)) {
			WebUtils.deleteAllCookies(request, response);
			
			SessionUser suser = getSessionUser();
			if(suser != null){
				ShardJedisTool.hdel(DefaultJedisKeyNS.uid2t, suser.getUserClsId(), suser.getLastLoginClientType() == null ? "" : suser.getLastLoginClientType().name());
				ShardJedisTool.del(DefaultJedisKeyNS.nsession, token);
			}
			
		}
	}
	
	private static void saveUid2Token(String userClsId, ClientType clientType, String token){
		ShardJedisTool.hset(DefaultJedisKeyNS.uid2t, userClsId, clientType == null ? "" : clientType.name(), token);
	}
	
	/*private static String getClientType() {

		if (ThreadUtil.isAppReq()) {
			return "app";
		} else if(ThreadUtil.isWxClient()){
			return "wx";
		} else if(ThreadUtil.isMobileClient()){
			return "mob";
		}

		return "web";
	}*/
	
	public static enum UserCls{
		bg //管理端用户
		,ft //前台用户
	}
	
	private static String buildUserClsId(UserCls userCls, long uid) {
		return userCls + "" + uid;
	}
	
	public static class SessionUser{
    	private long uid;
    	private String realName;
    	private String phone;
    	private String email;
    	private String headImg;
    	private String nickName;
    	private String posterCode; //当前登陆用户的推荐人的推荐码
    	private String myCode; //当前登陆用户的推广码
    	private String openId; //微信的openId
    	
    	private UserCls userCls;
    	
    	private String lastLoginTime;
    	private String lastLoginIP;
    	private EquipType lastLoginEquip;
    	private ClientType lastLoginClientType; 
    	
    	public String getShowName(){
    		String niceName = this.getRealName();
    		if(StringUtils.isBlank(niceName)){
    			niceName = this.getNickName();
    			if(StringUtils.isBlank(niceName)){
    				niceName = this.getPhone();
    				if(StringUtils.isBlank(niceName)){
    					niceName = this.getEmail();
    				}
    				
    				if(StringUtils.isNotBlank(niceName)){
    					niceName = StringUtil.stringHide(niceName);
    				}
    			}
    		}
    		
    		return niceName;
    	}
    	
		public void setUserCls(UserCls userCls) {
			this.userCls = userCls;
		}

		public UserCls getUserCls() {
			return userCls;
		}

		public long getUid() {
			return uid;
		}
		
		public String getUserClsId(){
			return buildUserClsId(this.userCls == null ? UserCls.bg : this.userCls, this.getUid());
		}
		
		public int getUidInt(){
			return (int)this.getUid();
		}
		public void setUid(long uid) {
			this.uid = uid;
		}
		public String getRealName() {
			return realName;
		}
		public void setRealName(String realName) {
			this.realName = realName;
		}
		public String getPhone() {
			return phone;
		}
		public void setPhone(String phone) {
			this.phone = phone;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getHeadImg() {
			return headImg;
		}
		public void setHeadImg(String headImg) {
			this.headImg = headImg;
		}
		public String getNickName() {
			return nickName;
		}
		public void setNickName(String nickName) {
			this.nickName = nickName;
		}
		public String getPosterCode() {
			return posterCode;
		}
		public void setPosterCode(String posterCode) {
			this.posterCode = posterCode;
		}
		public String getMyCode() {
			return myCode;
		}
		public void setMyCode(String myCode) {
			this.myCode = myCode;
		}
		public String getOpenId() {
			return openId;
		}
		public void setOpenId(String openId) {
			this.openId = openId;
		}
		public String getLastLoginTime() {
			return lastLoginTime;
		}
		public void setLastLoginTime(String lastLoginTime) {
			this.lastLoginTime = lastLoginTime;
		}
		public String getLastLoginIP() {
			return lastLoginIP;
		}
		public void setLastLoginIP(String lastLoginIP) {
			this.lastLoginIP = lastLoginIP;
		}
		public EquipType getLastLoginEquip() {
			return lastLoginEquip;
		}
		public void setLastLoginEquip(EquipType lastLoginEquip) {
			this.lastLoginEquip = lastLoginEquip;
		}

		public ClientType getLastLoginClientType() {
			return lastLoginClientType;
		}

		public void setLastLoginClientType(ClientType lastLoginClientType) {
			this.lastLoginClientType = lastLoginClientType;
		}
    	
    }

}
