package com.yisi.stiku.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtil {

	private final static Logger LOG = LoggerFactory.getLogger(ThreadUtil.class);

	// private static ThreadLocal<String> tokens = new ThreadLocal<String>();
	// //当前会话的token
	// private static ThreadLocal<Long> userIds = new ThreadLocal<Long>();
	// //用户的id
	// private static ThreadLocal<String> showUserNames = new
	// ThreadLocal<String>(); //用于显示的用户名
	// private static ThreadLocal<String> traceMsgIds = new
	// ThreadLocal<String>(); //用户标识某个请求的消息id

	public final static String USER_ID = "ui";
	public final static String USER_SHOW_NAME = "usn";
	public final static String SIGN_NAME = "sn";
	public final static String QQ = "qq";
	public final static String USER_TYPE = "tp";
	public final static String USER_TYPE_LOGON_PROJECT = "tplp";
	public final static String REAL_NAME = "rn";
	public final static String NICK = "nc";
	public final static String USER_ICON = "uic";
	public final static String SECTION = "sc";
	public final static String GRADE_YEAR = "gy";
	public final static String STYPE = "st";

	public final static String SCHOOL_ID = "sid";
	public final static String CLASS_ID = "cid";
	public final static String SCHOOL_NAME = "snm";
	public final static String CLASS_NAME = "cnm";

	public final static String ACL_CODE = "ac"; // 后台和老师用户才有的权限code
	public final static String TEACHER_ID = "tid"; // 老师表的ID

	public final static String LOGIN_IP = "lip";
	public final static String EQUIP_TYPE = "et"; // 设备类型，比如pc、android、iPhone、ipod、ipad等

	public final static String TOKEN = "tk"; // 需要被存入到cookie中的token的key
	public final static String LOGIN_TIME = "lt";

	// private final static String TOKEN = "tk";
	// private final static String USER_ID = "ui";
	// private final static String USER_NAME = "un";
	// private final static String USER_TYPE = "ut";
	private final static String TRACE_MSG_ID = "tmi";
	private final static String SRC_PROJECT_NAME = "spn";
	private final static String CLIENT_PROJECT_NAME = "cpn";
	private final static String SRC_IP = "si";
	private final static String CLIENT_IP = "ci";
	private final static String IS_APP_REQ = "iar"; // 是否为app请求

	private static ThreadLocal<Map<String, Object>> threadInfoMap = new ThreadLocal<Map<String, Object>>();

	public static void put(String key, Object val) {

		Map<String, Object> infoMap = threadInfoMap.get();
		if (infoMap == null) {
			synchronized (ThreadUtil.class) {
				infoMap = threadInfoMap.get();
				if (infoMap == null) {
					infoMap = new HashMap<String, Object>();
					threadInfoMap.set(infoMap);
				}
			}
		}

		infoMap.put(key, val);
	}

	public static void putIfAbsent(String key, Object val) {

		if (get(key) == null) {
			put(key, val);
		}
	}

	public static Map<String, Object> getThreadInfoMap() {

		if (threadInfoMap.get() == null) {
			return new HashMap<String, Object>();
		}
		return Collections.unmodifiableMap(threadInfoMap.get());
	}

	public static void putAllThreadInfo(Map<String, Object> infoMap) {

		if (infoMap != null && !infoMap.isEmpty()) {
			Map<String, Object> currMap = threadInfoMap.get();
			if (currMap == null) {
				synchronized (ThreadUtil.class) {
					currMap = threadInfoMap.get();
					if (currMap == null) {
						currMap = new HashMap<String, Object>();
						threadInfoMap.set(currMap);
					}
				}
			}
			currMap.putAll(infoMap);
		}
	}

	@SuppressWarnings("all")
	public static <T> T get(String key) {

		Map<String, Object> infoMap = threadInfoMap.get();

		return infoMap == null ? null : (T) infoMap.get(key);
	}

	public static void setToken(String token) {

		putIfAbsent(TOKEN, token);
	}

	public static String getToken() {

		return (String) get(TOKEN);
	}

	/**
	 * 设置是否为app请求的标识
	 */
	public static void setAppReqFlag() {

		putIfAbsent(IS_APP_REQ, true);
	}

	/**
	 * 
	 * @return 如果当前请求为app请求，则返回true
	 */
	public static boolean isAppReq() {

		Object val = get(IS_APP_REQ);
		return val != null && (Boolean) val;
	}

	public static void setUserInfo(long userId, String userName, int userType) {

		putIfAbsent(USER_ID, userId);
		putIfAbsent(USER_SHOW_NAME, userName);
		putIfAbsent(USER_TYPE, userType);
		// userIds.set(userId);
		// showUserNames.set(userName);
	}

	public static Long getUserId() {

		return get(USER_ID);
	}

	public static String getUserName() {

		return get(USER_SHOW_NAME);
	}

	public static Integer getUserType() {

		return get(USER_TYPE);
	}

	public static String getTraceMsgId() {

		return get(TRACE_MSG_ID);
	}

	public static void setSrcProject(String projectName) {

		putIfAbsent(SRC_PROJECT_NAME, projectName);
	}

	public static void setClientProject(String projectName) {

		put(CLIENT_PROJECT_NAME, projectName);
	}

	public static String getSrcProject() {

		return get(SRC_PROJECT_NAME);
	}

	public static String getClientProject() {

		return get(CLIENT_PROJECT_NAME);
	}

	public static void setSrcIP(String ip) {

		putIfAbsent(SRC_IP, ip);
	}

	public static void setClientIP(String ip) {

		put(CLIENT_IP, ip);
	}

	public static String getSrcIP() {

		return get(SRC_IP);
	}

	public static String getClientIP() {

		return get(CLIENT_IP);
	}

	public static void setTraceMsgId(String traceMsgId) {

		if (StringUtils.isNotBlank(traceMsgId)) {
			putIfAbsent(TRACE_MSG_ID, traceMsgId);
		} else {
			LOG.warn("traceMsgId has already set, and will ignore this operation.");
		}
	}

	public static void genTraceMsgId() {

		String traceId = (System.nanoTime() + RandomUtil.rand(100000)) + "";
		setTraceMsgId(traceId);
	}

	public static void clear() {

		threadInfoMap.remove();
	}
}
