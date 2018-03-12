package com.xyz.tools.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.bean.IUser;
import com.xyz.tools.common.constant.GlobalConstant;

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

	public final static String TOKEN_KEY = BaseConfig.getValue("sys.session.token.key", "tk"); // 需要被存入到cookie中的token的key
	public final static String LOGIN_TIME = "lt";
	private final static String USER_INFO = "uinfo"; //用户存储当前登录对象
	
	public final static String COOKIE_POSTER_CODE_KEY = "ptecd";
	
	public final static String OPENID_KEY = "oinm"; //openID
	
	public final static String REMINDME = "rmd"; //记住一周的标识
	
	public final static String RESPONSE_KEY = "resp";
	public final static String REQUEST_KEY = "req";
	public final static String CURR_DOMIAN_KEY = "domain";
	public final static String CURR_PORT_KEY = "port";
	private final static String SERVLET_URI_KEY = "uri";
	private final static String CURR_URL = "url";
	private final static String IS_WX_CLIENT_KEY = "wxcn";
	private final static String IS_MOBILE_CLIENT = "mobcln"; //是否为移动端
	private final static String IS_APP_REQ = "iar"; // 是否为app请求
	
	public final static String TRACE_MSG_ID = "tmi";
	public final static String SRC_PROJECT_KEY = "spn";
	
	private final static String SESSION_ID_KEY = "sid";
	private final static String CLIENT_PROJECT_NAME = "cpn";
	private final static String SRC_IP = "si";
	private final static String CLIENT_IP = "ci";
	private final static String EQUIP_TYPE = "et"; // 设备类型，比如pc、android、iPhone、ipod、ipad等
	
	private final static String REQ_CONTENT = "rcontent"; //存储request body内容的key
	
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
	
	public static String getCurrDomain(){
		return get(CURR_DOMIAN_KEY);
	}
	
	public static String getCurrUri() {
		return get(SERVLET_URI_KEY);
	}
	
	public static int getCurrPort(){
		return get(CURR_PORT_KEY);
	}
	
	public static String getCurrUrl(){
		return get(CURR_URL);
	}
	
	public static String getEquipType() {
		return get(EQUIP_TYPE);
	}
	
	public static void setEquipType(String equipType) {
		putIfAbsent(EQUIP_TYPE, equipType);
	}

	public static void setCurrUri(String servletUri) {
		putIfAbsent(SERVLET_URI_KEY, servletUri);
	}
	
	public static void setCurrUrl(String currUrl) {
		putIfAbsent(CURR_URL, currUrl);
	}
	
	public static void setCurrDomain(String currDomain){
		putIfAbsent(CURR_DOMIAN_KEY, currDomain);
	}
	
	public static void setCurrPort(int port){
		putIfAbsent(CURR_PORT_KEY, port);
	}
	
	public static void setReqContent(String content){
		putIfAbsent(REQ_CONTENT, content);
	}
	
	public static String getReqContent(){
		return get(REQ_CONTENT);
	}
	
	/**
	 * 
	 * @return 返回二级域名前缀（如果有多级域名，并且以www.开头，则忽略开头的www.部分）
	 * eg.
	 *   www.baoxianjie.net -> www
	 *   baoxianjie.net -> www
	 *   123.baoxianjie.net -> 123
	 *   www.123.baoxianjie.net -> 123
	 */
	public static String getDomainPrefix(){
		String domain = getCurrDomain();
		String prefix = "";
		if(StringUtils.isNotBlank(domain)){
			String[] parts = domain.split("\\."); 
			if(parts.length <= 2){
				prefix = "www";
			} else {
				prefix = StringUtils.join(parts, ".", 0, parts.length - 2);
				if(prefix.startsWith("www.")){
					prefix = prefix.substring(4);
				}
				if(prefix.startsWith("m.")){
					prefix = prefix.substring(2);
				}
				if("m".equals(prefix)){
					prefix = "www";
				}
			}
		}
		return  prefix; 
	}
	
	/**
	 * 用于判断前端url是否为pc请求。 管理端不要用
	 * @return
	 */
	public static boolean isPCDomain(){
//		if(GlobalConstant.IS_WEB_PC || GlobalConstant.IS_WEB_H5){
			String domain = getCurrDomain();
			
			return domain == null || !domain.startsWith("m.");
//		}
		
//		throw new BaseRuntimeException("NOT_SUPPORT", "本方法只支持在pc或者h5的web中使用");
	}
	
	/**
	 * 用于判断前端url是否为pc请求
	 * @return
	 */
	public static boolean isH5Domain(){
		
		return !isPCDomain();
	}
	
	public static String getViewPrefix(){
		return isPCDomain() ? "pc" : "h5";
	}
	
	@SuppressWarnings("all")
	public static <T> T get(String key) {

		Map<String, Object> infoMap = threadInfoMap.get();

		return infoMap == null ? null : (T) infoMap.get(key);
	}

	public static void setToken(String token) {

		putIfAbsent(TOKEN_KEY, token);
	}
	
	public static String getToken() {

		return (String) get(TOKEN_KEY);
	}
	
	public static void setSid(String sid){
		putIfAbsent(SESSION_ID_KEY, sid);
	}
	
	public static String getSid(){
		return (String)get(SESSION_ID_KEY);
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
	
	public static void setAppReq(boolean isAppReq) {
		putIfAbsent(IS_WX_CLIENT_KEY, isAppReq);
	}
	
	public static void setWxClient(boolean isWxClient) {
		putIfAbsent(IS_WX_CLIENT_KEY, isWxClient);
	}
	
	public static boolean isWxClient(){
		Object wxclient = get(IS_WX_CLIENT_KEY);
		
		return wxclient != null && Boolean.valueOf(wxclient.toString());
	}
	
	public static boolean isMobileClient(){
		Object wxclient = get(IS_MOBILE_CLIENT);
		
		return wxclient != null && Boolean.valueOf(wxclient.toString());
	}
	
	public static <T extends IUser> void setCurrUser(T userInfo){
		putIfAbsent(USER_INFO, userInfo);
	}
	
	public static <T extends IUser> T getCurrUser(){
		return get(USER_INFO);
	}

	/*public static void setUserInfo(int userId, String userName, String realName, Integer roleId, Integer acId) {

		putIfAbsent(USER_ID, userId);
		putIfAbsent(LOGIN_NAME, userName);
		putIfAbsent(REAL_NAME, realName);
		putIfAbsent(ROLE_NAME, roleId);
		put(ACID_NAME, acId);
		// userIds.set(userId);
		// showUserNames.set(userName);
	}*/
	
	public static Long getUid() {
        IUser currUser = ThreadUtil.getCurrUser();
		
		return currUser == null ? null : currUser.getUid();
	}

	public static Integer getUidInt() {
		IUser currUser = ThreadUtil.getCurrUser();
		
		return currUser == null ? null : currUser.getUidInt();
	}
	
	public static String getRealName() {
		IUser currUser = ThreadUtil.getCurrUser();
		
		return currUser == null ? null : currUser.getRealName();
	}
	
	public static String getPhone() {
		IUser currUser = ThreadUtil.getCurrUser();
		
		return currUser == null ? null : currUser.getPhone();
	}
	
	public static String getEmail() {
		IUser currUser = ThreadUtil.getCurrUser();
		
		return currUser == null ? null : currUser.getEmail();
	}
	
	public static String getShowName() {
		IUser currUser = ThreadUtil.getCurrUser();
		
		return currUser == null ? null : currUser.getShowName();
	}
	
	/*public static Integer getRoleId() {
		return get(ROLE_NAME);
	}
	
	public static Integer getAcId() {
		return get(ACID_NAME);
	}

	public static String getLoginName() {
		return get(LOGIN_NAME);
	}

	

	public static Integer getUserType() {
		return get(USER_TYPE);
	}*/

	public static void setSrcProject(String projectName) {

		putIfAbsent(SRC_PROJECT_KEY, projectName);
	}

	public static void setClientProject(String projectName) {

		put(CLIENT_PROJECT_NAME, projectName);
	}

	public static String getSrcProject() {

		return get(SRC_PROJECT_KEY);
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

	private static void setTraceMsgId(String traceMsgId) {

		String msg = null;
		if (StringUtils.isNotBlank(traceMsgId)) {
//			if(StringUtils.isBlank(getTraceMsgId())){
				putIfAbsent(TRACE_MSG_ID, traceMsgId);
//			} else {
//				msg = "traceMsgId has already set";
//			}
		} else {
			msg = "traceMsgId is null";
		}
		if(StringUtils.isNotBlank(msg)){
			String className = Thread.currentThread().getStackTrace()[2].getClassName();
			String methodName  = Thread.currentThread().getStackTrace()[2].getMethodName();
			LOG.warn(msg + ", and will ignore this operation for " + className + "." + methodName);
		}
	}
	
	/**
	 * 返回当前线程的唯一ID
	 * @return
	 */
	public static String getTraceMsgId() {
		return getTraceMsgId(null);
	}
	
	/**
	 * 如果当前线程中已经存在一个traceMsgId，则直接返回该traceMsgId
	 * 如果参数msgId不为空，则将msgId存入当前线程中，并返回该msgId; 否则为当前线程生成一个唯一ID并返回
	 * @param msgId
	 * @return
	 */
	public static String getTraceMsgId(String msgId){
		return getTraceMsgId(null, msgId);
	}
	
	/**
	 * 在msgId前边加个前缀，以便区分msg的来源
	 * @param prefix
	 */
	public static String getPrefixMsgId(String prefix){
		return getTraceMsgId(prefix, null);
	}
	
	private static String getTraceMsgId(String prefix, String msgId){
		String existMsgId = get(TRACE_MSG_ID);
		if(StringUtils.isNotBlank(existMsgId)){
			return existMsgId;
		}
		if(StringUtils.isNotBlank(msgId)){
			msgId = msgId.trim();
		} else {
			/*String ip = IPUtil.getLocalIp();
			String[] parts = ip.split("\\.");
			
			String ipsuffix = "";
			if(parts.length >= 4){
				ipsuffix = StringUtils.join(parts, "_", 2, 4);
			}*/
			prefix = StringUtils.isBlank(prefix) ? GlobalConstant.PROJECT_NAME_SUFFIX : prefix;
			String timestr = (System.currentTimeMillis() + "").substring(1);
			int random = RandomUtil.rand(10000);
			
			msgId = prefix + timestr + random;
		}
				
		setTraceMsgId(msgId);
		
		return msgId;
	}

	/*public static void genTraceMsgId() {
		genTraceMsgId("");
	}

	
	public static void genTraceMsgId(String prefix){
		String traceId = (System.nanoTime() + RandomUtil.rand(100000)) + "";
		setTraceMsgId((StringUtils.isBlank(prefix) ? "" : prefix) + traceId);
	}*/

	public static void clear() {

		threadInfoMap.remove();
	}
}
