package com.ujigu.secure.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.cache.constant.DefaultJedisKeyNS;
import com.ujigu.secure.cache.redis.ShardJedisTool;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.common.utils.EncryptTools;
import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.RandomUtil;
import com.ujigu.secure.common.utils.StringUtil;
import com.ujigu.secure.common.utils.ThreadUtil;

public class LogonUtil {
	
	private final static int WEEK_SECONDS = 7*24*3600;
	
	private final static String REMIND_ME_KEY = "_rme";
	private final static String REMIND_ME_VAL = "T";
	
	public static String storeSession(HttpServletRequest request, HttpServletResponse response, SessionUser suser, boolean remindMe){
		if(suser == null || suser.getUid() <= 0){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "系统异常，请重试或联系管理员！", "session信息不合法");
		}
		String clientIp = WebUtils.getClientIp(request);
		clientIp = StringUtils.isBlank(clientIp) ? IPUtil.getLocalIp() : clientIp;
		String equipType = WebUtils.getEquipType(request);
		long currTime = System.currentTimeMillis();
		int randCode = RandomUtil.rand(100000);
		String token = EncryptTools.encryptByMD5(clientIp + equipType + suser.getUid() + currTime + randCode);
		if(suser.getLastLoginTime() == null){
			suser.setLastLoginTime(DateUtil.getCurrentDateTimeStr());
		}
		if(suser.getLastLoginEquip() == null){
			suser.setLastLoginEquip(equipType);
		}
		if(suser.getLastLoginIP() == null){
			suser.setLastLoginIP(clientIp);
		}
		if(suser.getLastLoginClientType() == null){
			suser.setLastLoginClientType(getClientType());
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
		
		WebUtils.setHttpOnlyCookie(response, ThreadUtil.TOKEN, token,
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
	
	public static void continueSessionTTL() {
		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("NOT_LOGON", "用户尚未登陆");
		}
		
		try {
			ShardJedisTool.expire(DefaultJedisKeyNS.nsession, token, DefaultJedisKeyNS.session.getExpire());
			SessionUser suser = ThreadUtil.getUserInfo();
			if(suser != null){
				ShardJedisTool.expire(DefaultJedisKeyNS.uid2t, suser.getUserClsId(), DefaultJedisKeyNS.uid2t.getExpire());
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
				ShardJedisTool.hdel(DefaultJedisKeyNS.uid2t, suser.getUserClsId(), suser.getLastLoginClientType());
				ShardJedisTool.del(DefaultJedisKeyNS.nsession, token);
			}
			
		}
	}
	
	private static void saveUid2Token(String userClsId, String clientType, String token){
		ShardJedisTool.hset(DefaultJedisKeyNS.uid2t, userClsId, clientType, token);
	}
	
	private static String getClientType() {

		if (ThreadUtil.isAppReq()) {
			return "app";
		} else if(ThreadUtil.isWxClient()){
			return "wx";
		} else if(ThreadUtil.isMobileClient()){
			return "mob";
		}

		return "web";
	}
	
	public static enum UserCls{
		bg //后台用户
		,ft //前台用户
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
    	private String lastLoginEquip;
    	private String lastLoginClientType; 
    	
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
			return (this.userCls == null ? UserCls.bg.name() : this.userCls.name()) + this.getUid();
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
		public String getLastLoginEquip() {
			return lastLoginEquip;
		}
		public void setLastLoginEquip(String lastLoginEquip) {
			this.lastLoginEquip = lastLoginEquip;
		}

		public String getLastLoginClientType() {
			return lastLoginClientType;
		}

		public void setLastLoginClientType(String lastLoginClientType) {
			this.lastLoginClientType = lastLoginClientType;
		}
    	
    }

}
