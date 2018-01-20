package com.ujigu.secure.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.web.util.UserLoginUtil;
import com.ujigu.secure.web.util.WebUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class UserLoginInterceptor extends AbstractInterceptor {

	private final static Logger LOG = LoggerFactory
			.getLogger(UserLoginInterceptor.class);

	private Set<String> needBindUrls = new TreeSet<String>();
	
	public void setNeedBindUrls(Set<String> needBindUrls) {

		if (needBindUrls != null && !needBindUrls.isEmpty()) {
			needBindUrls.addAll(this.needBindUrls);
			this.needBindUrls.clear();
			this.needBindUrls.addAll(needBindUrls);
		}

	}
	
	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String token = ThreadUtil.getToken();

		String loginUrl = request.getContextPath() + BaseConfig.getValue("project.loginpage.url", "/user/port/login.do");
		if (StringUtils.isBlank(token)) {
			String requestUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
			handleError(request, response, "NOT_LOGIN", "请重新登录", requestUrl);
			return false;
		}

		try {
			Map<String, String> sessionInfoMap = UserLoginUtil.getSessionInfo();
			if (sessionInfoMap == null || sessionInfoMap.isEmpty()) {
				String requestUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
				handleError(request, response, "NOT_LOGIN", "请重新登录", requestUrl);
				return false;
			}

			initLoginInfo(sessionInfoMap); // 先将当前的session信息存入ThreadLocal，以提高常用信息的查询效率

			/*if (!allowAccess()) {
				handleError(request, response, "ACCESS_DENIED",
						"不允许访问的用户类型", "/error/403");
				return false;
			}*/

			// Long userId = sessionInfo.get(key);
			// String userName = LoginUtil.getUserName();
			// ThreadUtil.setUserInfo(userId, userName);

			String remindMe = sessionInfoMap.get(ThreadUtil.REMINDME);
			if(StringUtils.isBlank(remindMe) || !"true".equals(remindMe)){ //如果用户勾选了记住一周的话，此处就不用给session续期了
				UserLoginUtil.continueSessionTTL();
			}
			if(GlobalConstant.IS_WEB_H5 && needBind(requestUri) && !UserLoginUtil.hasBind()){
				handleError(request, response, "NOT_BIND", "请重新登录", loginUrl);
				return false;
			}
			return true;
		} catch (BaseRuntimeException bre) {
			String errorMsg = StringUtils.isNotBlank(bre.getFriendlyMsg()) ? bre.getFriendlyMsg() : "您因长时间未操作已退出系统，请重新登录";
			LOG.warn(bre.getMessage());
			loginUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
			errorMsg = URLEncoder.encode(errorMsg, "UTF-8");
			handleError(request, response, "NOT_LOGIN", errorMsg,
					loginUrl + (loginUrl.contains("?") ? "&errorMsg=" : "?errorMsg=") + errorMsg);
			return false;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		handleError(request, response, "SYS_ERROR", "系统发生错误，请重试！",
				loginUrl);
		return false;
	}
	
	private boolean needBind(String requestUri){
		if (needBindUrls != null && !needBindUrls.isEmpty()) {
			for (String excludeUri : needBindUrls) {
				if (urlMatcher.match(excludeUri, requestUri)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/*private String buildRefererUrl(HttpServletRequest request, String loginUrl) throws IOException {

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
	}*/

	private void handleError(HttpServletRequest request,
			HttpServletResponse response, String code,
			String errorMsg, String redirectUrl) throws IOException {

		if (ThreadUtil.isAppReq() || WebUtils.isAjax(request)) {
			WebUtils.writeJson(new ResultModel(code, errorMsg),
					request, response);
		} else {
			response.sendRedirect(redirectUrl);
		}
	}

	protected void initLoginInfo(Map<String, String> sessionInfoMap) {

		/*String koMsg = sessionInfoMap.get(LoginUtil.getKOMsgKey());
		if (StringUtils.isNotBlank(koMsg)) {
			throw new BaseRuntimeException("NOT_LOGIN", koMsg); // 如果当前session信息中有被kickoff的消息，则认为当前session已失效
		}*/
		Integer userId = Integer.valueOf(sessionInfoMap.get(ThreadUtil.USER_ID));
		String loginName = sessionInfoMap.get(ThreadUtil.LOGIN_NAME);
		String realName = sessionInfoMap.get(ThreadUtil.REAL_NAME);
		String niceName = sessionInfoMap.get(ThreadUtil.NICE_NAME);
		String roleIdStr = sessionInfoMap.get(ThreadUtil.ROLE_NAME);
		String phone = sessionInfoMap.get(ThreadUtil.PHONE);
		String email = sessionInfoMap.get(ThreadUtil.EMAIL);
		String headImg = sessionInfoMap.get(ThreadUtil.USER_ICON);
		String myCode = sessionInfoMap.get(ThreadUtil.MY_CODE);
		String posterCode = sessionInfoMap.get(ThreadUtil.POSTER_CODE);
		String wxopenId = sessionInfoMap.get(ThreadUtil.OPENID_NAME);
		Integer roleId = StringUtils.isNotBlank(roleIdStr) ? Integer.valueOf(roleIdStr) : null;
		String acIdStr = sessionInfoMap.get(ThreadUtil.ACID_NAME);
		Integer acId = StringUtils.isNotBlank(acIdStr) ? Integer.valueOf(acIdStr) : null;
		String adIdStr = sessionInfoMap.get(ThreadUtil.ADID_NAME);
		Integer adId = StringUtils.isNotBlank(adIdStr) ? Integer.valueOf(adIdStr) : null;
		String typeStr = sessionInfoMap.get(ThreadUtil.USER_TYPE);
		Integer type = StringUtils.isNotBlank(typeStr) ? Integer.valueOf(typeStr) : null;

		ThreadUtil.setUserInfo(userId, loginName, realName, roleId, acId);
		ThreadUtil.putIfAbsent(ThreadUtil.ADID_NAME, adId);
		ThreadUtil.putIfAbsent(ThreadUtil.USER_TYPE, type);
		ThreadUtil.putIfAbsent(ThreadUtil.NICE_NAME, niceName);
		ThreadUtil.putIfAbsent(ThreadUtil.MY_CODE, myCode);
		ThreadUtil.putIfAbsent(ThreadUtil.POSTER_CODE, posterCode);
		ThreadUtil.putIfAbsent(ThreadUtil.PHONE, phone);
		ThreadUtil.putIfAbsent(ThreadUtil.EMAIL, email);
		ThreadUtil.putIfAbsent(ThreadUtil.OPENID_NAME, wxopenId);
		ThreadUtil.putIfAbsent(ThreadUtil.USER_ICON, headImg);
	}
	
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (ThreadUtil.getUserId() != null && ThreadUtil.getUserId() > 0
				&& !"/user/login/doLogout".equals(request.getServletPath())) {
			request.setAttribute("userId",
					UserLoginUtil.getUserId());
			request.setAttribute("loginName",
					UserLoginUtil.getPhone());
			request.setAttribute("realName",
					UserLoginUtil.getRealName());
			request.setAttribute("niceName",
					UserLoginUtil.getNiceName());
			request.setAttribute("showName",
					UserLoginUtil.getShowName());
			request.setAttribute("myCode",
					UserLoginUtil.getMyCode());
			request.setAttribute("posterCode",
					UserLoginUtil.getPostCode());
			String userIcon = UserLoginUtil.getHeadImg();
			/*if(StringUtils.isNotBlank(userIcon)){
				if(!userIcon.startsWith("http://") && !userIcon.startsWith("https://")){
					userIcon = GlobalConstant.IMG_DOMAIN + (userIcon.startsWith("/") ? userIcon : "/" + userIcon);
				}
			}else{
				userIcon = request.getContextPath() + "/static/images/mryh_tu.png";
			}*/
			request.setAttribute("userIcon",
					userIcon);
			request.setAttribute("acId",
					ThreadUtil.getAcId());
			request.setAttribute("isAgent", ThreadUtil.getUserType() != null && 1 == ThreadUtil.getUserType());
			// String userTypeStr = sessionInfo.get(ThreadUtil.USER_TYPE);
		}
	}

	/**
	 * 
	 * @return 根据用户的类型和zk上的访问配置，决定是否用于该类型的用户访问本系统
	 */
	/*private boolean allowAccess() {

		String userType = LoginUtil.getUserType() + "";
		String[] allowUserTypes = BaseConfig.getValueArr(
				"access.allow.types");
		String[] denyUserTypes = BaseConfig.getValueArr(
				"access.deny.types");
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
	}*/

	/*public void setExcludeUrls(Set<String> excludeUrls) {

		if (excludeUrls != null && !excludeUrls.isEmpty()) {
			excludeUrls.addAll(this.excludeUrls);
			this.excludeUrls.clear();
			this.excludeUrls.addAll(excludeUrls);
		}

	}*/

	

}
