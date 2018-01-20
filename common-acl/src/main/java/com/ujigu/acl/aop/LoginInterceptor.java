package com.ujigu.acl.aop;


import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.web.filter.AbstractInterceptor;
import com.ujigu.secure.web.util.LogonUtil;
import com.ujigu.secure.web.util.LogonUtil.SessionUser;
import com.ujigu.secure.web.util.WebUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class LoginInterceptor extends AbstractInterceptor {

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String token = ThreadUtil.getToken();

		String loginUrl = BaseConfig.getValue("project.loginpage.url", "/sys/port/tologin.do");
		if (StringUtils.isBlank(token)) {
			String requestUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
			handleError(request, response, "NOT_LOGIN", "请重新登录", requestUrl);
			return false;
		}

		try {
			SessionUser currUser = LogonUtil.getSessionUser();
			initLoginInfo(currUser); // 先将当前的session信息存入ThreadLocal，以提高常用信息的查询效率
			if(!LogonUtil.isRemindMe(request)){
				LogonUtil.continueSessionTTL();
			}
			return true;
		} catch (BaseRuntimeException bre) {
			String errorMsg = StringUtils.isNotBlank(bre.getFriendlyMsg()) ? bre.getFriendlyMsg() : "您因长时间未操作已退出系统，请重新登录";
			LogUtils.warn(bre.getMessage());
			loginUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
			errorMsg = URLEncoder.encode(errorMsg, "UTF-8");
			handleError(request, response, "NOT_LOGIN", errorMsg,
					loginUrl + (loginUrl.contains("?") ? "&errorMsg=" : "?errorMsg=") + errorMsg);
			return false;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
		}

		handleError(request, response, "SYS_ERROR", "系统发生错误，请重试！",
				loginUrl);
		return false;
	}

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

	private void initLoginInfo(SessionUser currUser) {

		ThreadUtil.setUserInfo(currUser);
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (ThreadUtil.getUserInfo() != null) {
			request.setAttribute("suser", ThreadUtil.getUserInfo());
		}
	}

}
