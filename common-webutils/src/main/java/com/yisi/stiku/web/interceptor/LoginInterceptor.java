package com.yisi.stiku.web.interceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.common.utils.UserInfoUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.web.constant.WebConstant;
import com.yisi.stiku.web.util.LoginSesionUtil;
import com.yisi.stiku.web.util.OperationResult;
import com.yisi.stiku.web.util.OperationResult.OperResultType;
import com.yisi.stiku.web.util.WebUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

	private final static Logger LOG = LoggerFactory
			.getLogger(LoginInterceptor.class);

	private AntPathMatcher urlMatcher = new AntPathMatcher();

	private Set<String> excludeUrls = new TreeSet<String>();

	{
		excludeUrls.add("/images/**");
		excludeUrls.add("/js/**");
		excludeUrls.add("/css/**");
		excludeUrls.add("/img/**");
		excludeUrls.add("/assets/**");
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		String requestUri = request.getServletPath();
		// 如果为 /ping/pang 链接，则直接返回true，不管当前用户有没有登录
		if (WebConstant.PING_PANG_URI.equals(requestUri)) {
			// try{
			// Long userId = LoginSesionUtil.getUserId();
			// String userName = LoginSesionUtil.getUserName();
			//
			// ThreadUtil.setUserInfo(userId, userName);
			// LoginSesionUtil.continueSessionTTL();
			// }catch(Exception e){
			// LOG.debug(e.getMessage());
			// }
			return true;
		}

		if (excludeUrls != null && !excludeUrls.isEmpty()) {
			for (String excludeUri : excludeUrls) {
				if (urlMatcher.match(excludeUri, requestUri)) {
					return true;
				}
			}
		}

		String token = ThreadUtil.getToken();

		String loginUrl = WebConstant.getLoginPageUrl(request.getContextPath());
		if (StringUtils.isBlank(token)) {
			String requestUrl = buildRefererUrl(request, loginUrl);
			handleError(request, response, OperResultType.NOT_LOGIN, "请重新登录", requestUrl);
			return false;
		}

		try {
			Map<String, String> sessionInfoMap = LoginSesionUtil.getSessionInfo();
			if (sessionInfoMap == null || sessionInfoMap.isEmpty()) {
				String requestUrl = buildRefererUrl(request, loginUrl);
				handleError(request, response, OperResultType.NOT_LOGIN, "请重新登录", requestUrl);
				return false;
			}

			initLoginInfo(sessionInfoMap); // 先将当前的session信息存入ThreadLocal，以提高常用信息的查询效率

			WebConstant.setCommonParam2Request(request, WebConstant.getSchoolIdsForCurrUser(LoginSesionUtil.getUserType()));

			if (!allowAccess()) {
				handleError(request, response, OperResultType.ACCESS_DENIED,
						"不允许访问的用户类型", "/error/403");
				return false;
			}

			// Long userId = sessionInfo.get(key);
			// String userName = LoginSesionUtil.getUserName();
			// ThreadUtil.setUserInfo(userId, userName);

			LoginSesionUtil.continueSessionTTL();
			return true;
		} catch (BaseRuntimeException bre) {
			String errorMsg = StringUtils.isNotBlank(bre.getFriendlyMsg()) ? bre.getFriendlyMsg() : "您因长时间未操作已退出系统，请重新登录";
			LOG.warn(bre.getMessage());
			loginUrl = buildRefererUrl(request, loginUrl);
			errorMsg = URLEncoder.encode(errorMsg, "UTF-8");
			handleError(request, response, OperResultType.NOT_LOGIN, errorMsg,
					loginUrl + (loginUrl.contains("?") ? "&errorMsg=" : "?errorMsg=") + errorMsg);
			return false;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		handleError(request, response, OperResultType.SYS_ERROR, "系统发生错误，请重试！",
				loginUrl);
		return false;
	}

	private String buildRefererUrl(HttpServletRequest request, String loginUrl) throws IOException {

		String queryStr = StringUtils.isNotBlank(request.getQueryString()) ? "?"
				+ request.getQueryString()
				: "";
		String requestUrl = request.getRequestURL().toString();
		String encodeUrl = URLEncoder
				.encode(requestUrl + queryStr, "UTF-8");

		return loginUrl
				+ (StringUtils.isNotBlank(encodeUrl) ? "?referer="
						+ URLEncoder.encode(encodeUrl, "UTF-8")
						: "");
	}

	private void handleError(HttpServletRequest request,
			HttpServletResponse response, OperResultType operType,
			String errorMsg, String redirectUrl) throws IOException {

		if (ThreadUtil.isAppReq() || WebUtils.isAjax(request)) {
			WebUtils.writeJson(OperationResult.buildResult(operType, errorMsg),
					request, response);
		} else {
			response.sendRedirect(redirectUrl);
		}
	}

	private void initLoginInfo(Map<String, String> sessionInfoMap) {

		String koMsg = sessionInfoMap.get(LoginSesionUtil.getKOMsgKey());
		if (StringUtils.isNotBlank(koMsg)) {
			throw new BaseRuntimeException("NOT_LOGIN", koMsg); // 如果当前session信息中有被kickoff的消息，则认为当前session已失效
		}
		Long userId = Long.valueOf(sessionInfoMap.get(ThreadUtil.USER_ID));
		String userName = sessionInfoMap.get(ThreadUtil.USER_SHOW_NAME);
		Integer userType = Integer.valueOf(sessionInfoMap.get(ThreadUtil.USER_TYPE));

		ThreadUtil.setUserInfo(userId, userName, userType);
		ThreadUtil.putIfAbsent(ThreadUtil.SIGN_NAME,
				sessionInfoMap.get(ThreadUtil.SIGN_NAME));
		ThreadUtil.putIfAbsent(ThreadUtil.NICK,
				sessionInfoMap.get(ThreadUtil.NICK));
		ThreadUtil.putIfAbsent(ThreadUtil.REAL_NAME,
				sessionInfoMap.get(ThreadUtil.REAL_NAME));
		ThreadUtil.putIfAbsent(ThreadUtil.USER_TYPE_LOGON_PROJECT,
				sessionInfoMap.get(ThreadUtil.USER_TYPE_LOGON_PROJECT));

		if (UserInfoUtil.isStudent(userType)) {
			ThreadUtil.putIfAbsent(ThreadUtil.USER_ICON,
					sessionInfoMap.get(ThreadUtil.USER_ICON));
			String schoolIdStr = sessionInfoMap.get(ThreadUtil.SCHOOL_ID);
			if (StringUtils.isNotBlank(schoolIdStr)) {
				ThreadUtil.putIfAbsent(ThreadUtil.SCHOOL_ID, Long.valueOf(schoolIdStr));
			}
			ThreadUtil.putIfAbsent(ThreadUtil.SCHOOL_NAME,
					sessionInfoMap.get(ThreadUtil.SCHOOL_NAME));
			String classIdStr = sessionInfoMap.get(ThreadUtil.CLASS_ID);
			if (StringUtils.isNotBlank(classIdStr)) {
				ThreadUtil.putIfAbsent(ThreadUtil.CLASS_ID, Long.valueOf(classIdStr));
			}
			ThreadUtil.putIfAbsent(ThreadUtil.CLASS_NAME,
					sessionInfoMap.get(ThreadUtil.CLASS_NAME));
			ThreadUtil.putIfAbsent(
					ThreadUtil.SECTION,
					StringUtils.isNotBlank(sessionInfoMap.get(ThreadUtil.SECTION)) ? Integer.valueOf(sessionInfoMap
							.get(ThreadUtil.SECTION)) : null);
			ThreadUtil.putIfAbsent(
					ThreadUtil.GRADE_YEAR,
					StringUtils.isNotBlank(sessionInfoMap.get(ThreadUtil.GRADE_YEAR)) ? Integer.valueOf(sessionInfoMap
							.get(ThreadUtil.GRADE_YEAR)) : null);
			ThreadUtil.putIfAbsent(
					ThreadUtil.STYPE,
					StringUtils.isNotBlank(sessionInfoMap.get(ThreadUtil.STYPE)) ? Integer.valueOf(sessionInfoMap
							.get(ThreadUtil.STYPE)) : null);
		} else {
			ThreadUtil.putIfAbsent(ThreadUtil.ACL_CODE,
					sessionInfoMap.get(ThreadUtil.ACL_CODE));
			if (UserInfoUtil.isTeacher(userType)) {
				String teacherIdStr = sessionInfoMap.get(ThreadUtil.TEACHER_ID);
				if (StringUtils.isNotBlank(teacherIdStr)) {
					ThreadUtil.putIfAbsent(ThreadUtil.TEACHER_ID, Long.valueOf(teacherIdStr));
				}
			}
		}

	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (ThreadUtil.getUserId() != null && ThreadUtil.getUserId() > 0
				&& !"/user/login/doLogout".equals(request.getServletPath())) {
			request.setAttribute("userId",
					ThreadUtil.getUserId());
			request.setAttribute("userName",
					ThreadUtil.getUserName());
			request.setAttribute("realName",
					ThreadUtil.get(ThreadUtil.REAL_NAME));
			request.setAttribute("userIcon",
					ThreadUtil.get(ThreadUtil.USER_ICON));
			request.setAttribute("userType",
					ThreadUtil.getUserType());
			// String userTypeStr = sessionInfo.get(ThreadUtil.USER_TYPE);
			if (UserInfoUtil.isStudent(ThreadUtil.getUserType())) {
				request.setAttribute("currStdSchoolId",
						ThreadUtil.get(ThreadUtil.SCHOOL_ID));
				request.setAttribute("currStdSchoolName",
						ThreadUtil.get(ThreadUtil.SCHOOL_NAME));
				request.setAttribute("currStdClassId",
						ThreadUtil.get(ThreadUtil.CLASS_ID));
				request.setAttribute("currStdCclassName",
						ThreadUtil.get(ThreadUtil.CLASS_NAME));
			} else if (UserInfoUtil.isTeacher(ThreadUtil.getUserType())) {
				request.setAttribute(ThreadUtil.TEACHER_ID, ThreadUtil.get(ThreadUtil.TEACHER_ID));
			}

		}
	}

	/**
	 * 
	 * @return 根据用户的类型和zk上的访问配置，决定是否用于该类型的用户访问本系统
	 */
	private boolean allowAccess() {

		String userType = LoginSesionUtil.getUserType() + "";
		String[] allowUserTypes = ConfigOnZk.getValueArr(
				ZkConstant.APP_ZK_PATH, "access.allow.types");
		String[] denyUserTypes = ConfigOnZk.getValueArr(
				ZkConstant.APP_ZK_PATH, "access.deny.types");
		// 如果没有定义白名单或者黑名单，则默认允许访问
		if (allowUserTypes == null && denyUserTypes == null) {
			return true;
		}

		return containsUserType(allowUserTypes, userType)
				|| !containsUserType(denyUserTypes, userType);
	}

	private boolean containsUserType(String[] userTypes, String userType) {

		if (userTypes == null) {
			return false;
		}

		return Arrays.asList(userTypes).contains(userType);
	}

	public void setExcludeUrls(Set<String> excludeUrls) {

		if (excludeUrls != null && !excludeUrls.isEmpty()) {
			excludeUrls.addAll(this.excludeUrls);
			this.excludeUrls.clear();
			this.excludeUrls.addAll(excludeUrls);
		}

	}

}
