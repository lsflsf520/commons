package com.ujigu.secure.web.util;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 该类已被UserLoginUtil取代
 * 
 * @author shangfeng
 *
 */
@Deprecated
public class LoginSesionUtil {

//	private final static Logger LOG = LoggerFactory.getLogger(LoginSesionUtil.class);
//
//	private final static String CLIENT_TOKEN_SUFFIX = "-tk";
//	private final static String CLIENT_EQUIP_SUFFIX = "-tp";

	/**
	 * 
	 * @param sessionMap
	 * @return session存储成功之后，返回该session的token值，用于写回到cookie中
	 */
	public static String storeSession(Map<String, Object> sessionMap, HttpServletResponse response) {

		return UserLoginUtil.storeSession(sessionMap, response, false);
	}

	/**
	 * 
	 * @param response
	 */
	public static void removeSession(HttpServletRequest request, HttpServletResponse response) {

		UserLoginUtil.removeSession(request, response);
	}
	
	public static Map<String, String> getSessionInfo() {

		return UserLoginUtil.getSessionInfo();
	}

	/**
	 * 
	 * @return 根据当前用户登录的客户端类型，返回存储被kickoff原因的key
	 */
	public static String getKOMsgKey() {

		return UserLoginUtil.getKOMsgKey();
	}


	public static void add2Session(String key, Serializable data){
		UserLoginUtil.add2Session(key, data);
	}

	public static void updateSessionInfo(Map<String, String> sessionInfoMap) {
		UserLoginUtil.updateSessionInfo(sessionInfoMap);
	}

	/**
	 * 顺延session的失效时间
	 */
	public static void continueSessionTTL() {

		UserLoginUtil.continueSessionTTL();
	}

	/**
	 * 
	 * @return 如果用户已经登录，则返回true，否则返回false
	 */
	public static boolean hasLogon() {

		return UserLoginUtil.hasLogon();
	}

	/**
	 * 
	 * @return
	 */
	public static int getUserId() {

		return UserLoginUtil.getUserId();
	}


	/**
	 * 
	 * @return 返回可以在互联网上显示的名称
	 */
	public static String getUserName() {

		return UserLoginUtil.getRealName();
	}

	/**
	 * 
	 * @return 返回用户的头像
	 */
	public static String getUserIcon() {

		return UserLoginUtil.getHeadImg();
	}
	
	/**
	 * 
	 * @return 返回用户的头像
	 */
	public static Integer getRoleId() {

		return UserLoginUtil.getRoleId();
	}
	
	/**
	 * 
	 * @return 返回用户所属代理公司ID
	 */
	public static Integer getAcId() {

		return UserLoginUtil.getAcId();
	}
	
	/**
	 * 
	 * @return 返回用户所属代理机构ID
	 */
	public static Integer getAdId() {

		return UserLoginUtil.getAdId();
	}

	/**
	 * 
	 * @return 返回用户的真实姓名
	 */
	public static String getRealName() {

		return UserLoginUtil.getRealName();
	}
	
	public static String getSessionValue(String key){
		return UserLoginUtil.getSessionValue(key);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getToken(HttpServletRequest request) {

		return UserLoginUtil.getToken(request);
	}

	/**
	 * 以sessionId为key，验证码为值存储到redis中，在用户登录前(即登录页、注册页)和初始密码重置页面用得着
	 * 
	 * @param request
	 */
	public static void saveTmpSessionInfo(HttpServletRequest request, String code, String mobileOrEmail) {

		UserLoginUtil.saveTmpSessionInfo(request, code, mobileOrEmail);
	}

	/**
	 * 
	 * @param request
	 * @param code
	 * @return 判断验证码是否正确，如果正确，则返回true；否则返回false
	 */
	public static boolean validateCode(HttpServletRequest request, String code, String mobileOrEmail) {

		return UserLoginUtil.validateCode(request, code, mobileOrEmail);
	}

	/**
	 * 
	 * @param request
	 * @return 返回存储验证码时，一起存入的邮箱或者手机号
	 */
	public static String getMobileOrEmail(HttpServletRequest request) {
		
        return UserLoginUtil.getMobileOrEmail(request);
	}

}
