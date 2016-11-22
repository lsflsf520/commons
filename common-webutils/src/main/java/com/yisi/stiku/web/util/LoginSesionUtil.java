package com.yisi.stiku.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.cache.constant.DefaultJedisKeyNS;
import com.yisi.stiku.cache.redis.ShardJedisTool;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.EncryptTools;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.common.utils.RandomUtil;
import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;

/**
 * 跟登录相关的session操作工具，包含存储session信息和获取session信息等
 * 
 * @author shangfeng
 *
 */
public class LoginSesionUtil {

	private final static Logger LOG = LoggerFactory.getLogger(LoginSesionUtil.class);

	private final static String CLIENT_TOKEN_SUFFIX = "-tk";
	private final static String CLIENT_EQUIP_SUFFIX = "-tp";

	/**
	 * 
	 * @param sessionMap
	 * @return session存储成功之后，返回该session的token值，用于写回到cookie中
	 */
	public static String storeSession(Map<String, Object> sessionMap, HttpServletResponse response) {

		if (sessionMap == null || sessionMap.get(ThreadUtil.USER_ID) == null
				|| sessionMap.get(ThreadUtil.USER_SHOW_NAME) == null) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "系统异常，请重试或联系管理员！", "session信息不合法");
		}
		String clientIp = sessionMap.get(ThreadUtil.LOGIN_IP) == null ? IPUtil.getLocalIp() : sessionMap.get(
				ThreadUtil.LOGIN_IP).toString();
		String equipType = sessionMap.get(ThreadUtil.EQUIP_TYPE).toString();
		String userId = sessionMap.get(ThreadUtil.USER_ID).toString();
		long currTime = System.currentTimeMillis();
		int randCode = RandomUtil.rand(100000);

		String token = EncryptTools.EncryptByMD5(clientIp + equipType + userId + currTime + randCode);
		sessionMap.put(ThreadUtil.LOGIN_TIME, currTime + "");

		boolean success = ShardJedisTool.hmset(DefaultJedisKeyNS.session, token, sessionMap);
		if (!success) {
			throw new BaseRuntimeException("SESSION_STORE_ERROR", "系统异常，请重试或联系管理员！", "session存储失败");
		}

		Map<String, Object> uidTkMap = new HashMap<String, Object>();
		String clientType = getClientType();
		uidTkMap.put(clientType + CLIENT_TOKEN_SUFFIX, token);
		uidTkMap.put(clientType + CLIENT_EQUIP_SUFFIX, equipType);
		try {
			ShardJedisTool.hmset(DefaultJedisKeyNS.uid_token, userId, uidTkMap);
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}

		WebUtils.setCookieValue(response, ThreadUtil.TOKEN, token,
				ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "cookie.root.domain", ".17daxue.com"), -1); // cookie默认24小时后失效

		return token;
	}

	/**
	 * 
	 * @param response
	 */
	public static void removeSession(HttpServletRequest request, HttpServletResponse response) {

		if (StringUtils.isNotBlank(ThreadUtil.getToken())) {
			ShardJedisTool.del(DefaultJedisKeyNS.session, ThreadUtil.getToken()); // 先删除缓存中的session信息
			String clientType = getClientType();
			ShardJedisTool.hdel(DefaultJedisKeyNS.uid_token, ThreadUtil.getUserId(), clientType + CLIENT_TOKEN_SUFFIX,
					clientType + CLIENT_EQUIP_SUFFIX);
		}
		WebUtils.deleteCookie(ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "cookie.root.domain", ".17daxue.com"), request,
				response, ThreadUtil.TOKEN); // 再删除掉浏览器中的cookie信息
	}

	/**
	 * 
	 * @param userId
	 *            强制某登录用户退出登录
	 */
	public static void kickOff(long userId) {

		try {
			Map<String, String> sessionInfoMap = ShardJedisTool.hgetAll(DefaultJedisKeyNS.uid_token, userId);
			if (sessionInfoMap != null && !sessionInfoMap.isEmpty()) {
				for (Entry<String, String> entry : sessionInfoMap.entrySet()) {
					if (StringUtils.isNotBlank(entry.getKey()) && entry.getKey().endsWith(CLIENT_TOKEN_SUFFIX)) {
						String token = entry.getValue();
						Map<String, Object> kickoffMsgMap = new HashMap<String, Object>();
						if (StringUtils.isNotBlank(token)) {
							// ShardJedisTool.del(DefaultJedisKeyNS.session,
							// token);
							String ctype = entry.getKey().replace(CLIENT_TOKEN_SUFFIX, "");
							kickoffMsgMap.put(getKOMsgKey(ctype), "你的账号已被管理员强制退出！");
						}
						ShardJedisTool.hmset(DefaultJedisKeyNS.session, token, kickoffMsgMap); // 在session中被暂存被kickoff的原因
						// ShardJedisTool.hset(DefaultJedisKeyNS.uid_token,
						// userId, ctype + "_ko",
						// "你的账号在其它地方登录了，如非本人操作，请尽快修改密码！");
						// //在session中被暂存被kickoff的原因
					}
				}
			}
			// ShardJedisTool.del(DefaultJedisKeyNS.uid_token, userId);
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

	/**
	 * 如果指定的userId已经登录系统，则抛出BaseRuntimeException异常
	 * 
	 * @param userId
	 */
	public static void checkLoginState(long userId) {

		String token = null;
		try {
			token = ShardJedisTool.hget(DefaultJedisKeyNS.uid_token, userId, getClientType() + CLIENT_TOKEN_SUFFIX);
			if (StringUtils.isNotBlank(token)) {
				// ShardJedisTool.del(DefaultJedisKeyNS.session, token);
				// //先删除缓存中的session信息
				ShardJedisTool.hset(DefaultJedisKeyNS.session, token, getKOMsgKey(), "你的账号在其它地方登录了，如非本人操作，请尽快修改密码！"); // 在session中被暂存被kickoff的原因
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}

		// if(StringUtils.isNotBlank(token) &&
		// ShardJedisTool.exists(DefaultJedisKeyNS.session, token)){
		// throw new BaseRuntimeException("REPEAT_LOGIN",
		// "你的账号正在使用，请不要重复登录！如果不是本人操作请联系所在校区的学习教练。", "userId " + userId +
		// " has logon.");
		// }
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
	private static String getClientType() {

		if (ThreadUtil.isAppReq()) {
			return "app";
		}

		return "web";
	}

	public static void updateSessionInfo(Map<String, Object> sessionInfoMap) {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}
		boolean success = ShardJedisTool.hmset(DefaultJedisKeyNS.session, token, sessionInfoMap);
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
			ShardJedisTool.expire(DefaultJedisKeyNS.uid_token, ThreadUtil.getUserId(),
					DefaultJedisKeyNS.uid_token.getExpire());
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
			return getUserId() > 0 && StringUtils.isNotBlank(token)
					&& StringUtils.isBlank(ShardJedisTool.hget(DefaultJedisKeyNS.session, token, getKOMsgKey()));
		} catch (BaseRuntimeException e) {
			return false;
		}
	}

	/**
	 * 
	 * @return
	 */
	public static long getUserId() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}
		Long userId = ThreadUtil.getUserId();
		if (userId != null) {
			return userId;
		}

		String userIdStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.USER_ID);

		if (StringUtils.isNotBlank(userIdStr)) {
			return Long.valueOf(userIdStr);
		}

		throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录或session已失效");
	}

	/**
	 * 
	 * @return 只有老师角色的用户才可以调用该方法，否则会抛出异常
	 */
	public static long getTeacherId() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		Long teacherId = ThreadUtil.get(ThreadUtil.TEACHER_ID);
		if (teacherId != null) {
			return teacherId;
		}

		String teacherIdStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.TEACHER_ID);

		if (StringUtils.isNotBlank(teacherIdStr)) {
			return Long.valueOf(teacherIdStr);
		}

		throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录或session已失效");
	}

	/**
	 * 
	 * @return 返回可以在互联网上显示的名称
	 */
	public static String getUserName() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		String userName = ThreadUtil.getUserName();
		if (StringUtils.isNotBlank(userName)) {
			return userName;
		}

		userName = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.USER_SHOW_NAME);

		if (StringUtils.isNotBlank(userName)) {
			return userName;
		}

		throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录或session已失效");
	}

	/**
	 * 
	 * @return 返回可以在互联网上显示的名称
	 */
	public static String getSignName() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		String signName = ThreadUtil.get(ThreadUtil.SIGN_NAME);
		if (StringUtils.isNotBlank(signName)) {
			return signName;
		}

		signName = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.SIGN_NAME);

		if (StringUtils.isNotBlank(signName)) {
			return signName;
		}

		throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录或session已失效");
	}

	/**
	 * 
	 * @return 返回当前登录用户的类型
	 * @see com.yisi.stiku.basedata.rpc.constant.UserType
	 */
	public static int getUserType() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		if (ThreadUtil.getUserType() != null) {
			return ThreadUtil.getUserType();
		}

		String userType = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.USER_TYPE);
		if (StringUtils.isNotBlank(userType)) {
			return Integer.valueOf(userType);
		}
		throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录或session已失效");
	}

	/**
	 * 
	 * @return 返回当前用户的昵称
	 */
	public static String getUserTypeProjectName() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		String userTypeProjectName = ThreadUtil.get(ThreadUtil.USER_TYPE_LOGON_PROJECT);
		if (StringUtils.isNotBlank(userTypeProjectName)) {
			return userTypeProjectName;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.USER_TYPE_LOGON_PROJECT);
	}

	/**
	 * 
	 * @return 返回当前用户的昵称
	 */
	public static String getNick() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		String nick = ThreadUtil.get(ThreadUtil.NICK);
		if (StringUtils.isNotBlank(nick)) {
			return nick;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.NICK);
	}

	/**
	 * 
	 * @return 返回用户的头像
	 */
	public static String getUserIcon() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			throw new BaseRuntimeException("DATA_NOT_EXIST", "用户尚未登录");
		}

		String userIcon = ThreadUtil.get(ThreadUtil.USER_ICON);
		if (StringUtils.isNotBlank(userIcon)) {
			return userIcon;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.USER_ICON);
	}

	/**
	 * 
	 * @return 返回用户的真实姓名
	 */
	public static String getRealName() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		String realName = ThreadUtil.get(ThreadUtil.REAL_NAME);
		if (StringUtils.isNotBlank(realName)) {
			return realName;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.REAL_NAME);
	}

	/**
	 * 
	 * @return 返回用户所在的学校id
	 */
	public static Long getSchoolId() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		Long schoolId = ThreadUtil.get(ThreadUtil.SCHOOL_ID);
		if (schoolId != null) {
			return schoolId;
		}

		String schoolIdStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.SCHOOL_ID);
		return StringUtils.isBlank(schoolIdStr) ? null : Long.valueOf(schoolIdStr);
	}

	/**
	 * 
	 * @return 返回用户的学校名称
	 */
	public static String getSchoolName() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		String schoolName = ThreadUtil.get(ThreadUtil.SCHOOL_NAME);
		if (StringUtils.isNotBlank(schoolName)) {
			return schoolName;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.SCHOOL_NAME);
	}

	/**
	 * 
	 * @return 返回用户的班级id
	 */
	public static Long getClassId() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		Long classId = ThreadUtil.get(ThreadUtil.CLASS_ID);
		if (classId != null) {
			return classId;
		}

		String classIdStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.CLASS_ID);
		return StringUtils.isBlank(classIdStr) ? null : Long.valueOf(classIdStr);
	}

	/**
	 * 
	 * @return 返回用户的班级名称
	 */
	public static String getClassName() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		String className = ThreadUtil.get(ThreadUtil.CLASS_NAME);
		if (StringUtils.isNotBlank(className)) {
			return className;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.CLASS_NAME);
	}

	/**
	 * 
	 * @return 返回学生的分数段
	 */
	public static Integer getStudentSection() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		Integer section = ThreadUtil.get(ThreadUtil.SECTION);
		if (section != null) {
			return section;
		}

		String sectionStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.SECTION);
		return StringUtils.isBlank(sectionStr) ? null : Integer.valueOf(sectionStr);
	}

	/**
	 * 
	 * @return 返回学生的入学年份
	 */
	public static Integer getGradeYear() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		Integer gradeYear = ThreadUtil.get(ThreadUtil.GRADE_YEAR);
		if (gradeYear != null) {
			return gradeYear;
		}

		String gradeYearStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.GRADE_YEAR);
		return StringUtils.isBlank(gradeYearStr) ? null : Integer.valueOf(gradeYearStr);
	}

	/**
	 * 
	 * @return 返回学生的文理科
	 */
	public static Integer getSType() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		Integer stype = ThreadUtil.get(ThreadUtil.STYPE);
		if (stype != null) {
			return stype;
		}

		String stypeStr = ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.STYPE);
		return StringUtils.isBlank(stypeStr) ? null : Integer.valueOf(stypeStr);
	}

	/**
	 * 
	 * @return 返回当前用户的aclcode字符串；如果没有aclcode就返回null
	 */
	public static String getAclCodeStr() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		String aclCode = ThreadUtil.get(ThreadUtil.ACL_CODE);
		if (StringUtils.isNotBlank(aclCode)) {
			return aclCode;
		}

		return ShardJedisTool.hget(DefaultJedisKeyNS.session, token, ThreadUtil.ACL_CODE);
	}

	public static Map<String, String> getSessionInfo() {

		String token = ThreadUtil.getToken();
		if (StringUtils.isBlank(token)) {
			return null;
		}

		return ShardJedisTool.hgetAll(DefaultJedisKeyNS.session, token);
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

		ShardJedisTool.hmset(DefaultJedisKeyNS.vc,
				key, map);
	}

	/**
	 * 
	 * @param request
	 * @param code
	 * @return 判断验证码是否正确，如果正确，则返回true；否则返回false
	 */
	public static boolean validateCode(HttpServletRequest request, String code) {

		String key = request.getSession().getId();

		return StringUtils.isNotBlank(code) && code.equalsIgnoreCase(ShardJedisTool.hget(DefaultJedisKeyNS.vc,
				key, "code"));
	}

	/**
	 * 
	 * @param request
	 * @return 返回存储验证码时，一起存入的邮箱或者手机号
	 */
	public static String getMobileOrEmail(HttpServletRequest request) {

		String key = request.getSession().getId();

		return ShardJedisTool.hget(DefaultJedisKeyNS.vc,
				key, "mobileOrEmail");
	}

}
