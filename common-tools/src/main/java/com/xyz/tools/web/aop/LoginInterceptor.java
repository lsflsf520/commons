package com.xyz.tools.web.aop;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.ModelAndView;

import com.xyz.tools.common.bean.IUser;
import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.ThreadUtil;
import com.xyz.tools.web.util.UserLoginHelper;
import com.xyz.tools.web.util.WebUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class LoginInterceptor extends AbstractInterceptor implements ApplicationContextAware {

	private final static Logger LOG = LoggerFactory.getLogger(LoginInterceptor.class);

	private UserLoginHelper userLoginHelper;

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String token = ThreadUtil.getToken();

		String loginUrl = request.getContextPath()
				+ BaseConfig.getValue("project.loginpage.url", "/sys/port/loginPage.do");
		if (StringUtils.isBlank(token)) {
			String requestUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
			handleError(request, response, "NOT_LOGIN", "请重新登录", requestUrl);
			return false;
		}

		try {
			IUser user = userLoginHelper.getSessionUser();
			if (user == null || user.getUid() <= 0) {
				String requestUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
				handleError(request, response, "NOT_LOGIN", "请重新登录", requestUrl);
				return false;
			}

			initLoginInfo(user); // 先将当前的session信息存入ThreadLocal，以提高常用信息的查询效率
			userLoginHelper.continueSessionTTL();
			return true;
		} catch (BaseRuntimeException bre) {
			String errorMsg = StringUtils.isNotBlank(bre.getFriendlyMsg()) ? bre.getFriendlyMsg()
					: "您因长时间未操作已退出系统，请重新登录";
			LOG.warn(bre.getMessage());
			loginUrl = WebUtils.buildRedirectUrl(request, response, loginUrl);
			errorMsg = URLEncoder.encode(errorMsg, "UTF-8");
			handleError(request, response, "NOT_LOGIN", errorMsg,
					loginUrl + (loginUrl.contains("?") ? "&errorMsg=" : "?errorMsg=") + errorMsg);
			return false;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		handleError(request, response, "SYS_ERROR", "系统发生错误，请重试！", loginUrl);
		return false;
	}

	private void handleError(HttpServletRequest request, HttpServletResponse response, String code, String errorMsg,
			String redirectUrl) throws IOException {

		if (ThreadUtil.isAppReq() || WebUtils.isAjax(request)) {
			WebUtils.writeJson(new ResultModel(code, errorMsg), request, response);
		} else {
			response.sendRedirect(redirectUrl);
		}
	}

	private void initLoginInfo(IUser user) {

		/*
		 * String koMsg = sessionInfoMap.get(LoginSesionUtil.getKOMsgKey()); if
		 * (StringUtils.isNotBlank(koMsg)) { throw new BaseRuntimeException("NOT_LOGIN",
		 * koMsg); // 如果当前session信息中有被kickoff的消息，则认为当前session已失效 }
		 */

		ThreadUtil.setCurrUser(user);

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		IUser user = ThreadUtil.getCurrUser();
		if (user != null) {
			request.setAttribute("currUser", user);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		try {
			userLoginHelper = context.getBean(UserLoginHelper.class);
		} catch (Exception e) {
			LogUtils.warn("not found bean for UserLoginHelper in ApplicationContext");
		}
	}

}
