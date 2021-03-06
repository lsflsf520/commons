package com.xyz.tools.web.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.web.util.UserLoginHelper;

/**
 * 根据当前sessionId来校验用户输入的邮箱验证码 _code_ 是否正确
 * 
 * @author lsf
 *
 */
public class EmailCodeValidInterceptor extends AbstractInterceptor implements ApplicationContextAware {

	private UserLoginHelper userLoginHelper;

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String code = request.getParameter("_code_");
		// String imgKey = request.getParameter("imgKey");
		if (StringUtils.isBlank(code)) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "邮箱验证码不能为空");
		}
		String mobileOrEmail = request.getParameter("email");
		if (StringUtils.isBlank(mobileOrEmail)) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "用于校验的邮箱地址不能为空");
		}

		boolean result = userLoginHelper.validateCode(request, code, mobileOrEmail);
		if (!result) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "邮箱验证码不正确");
		}
		return result;
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
