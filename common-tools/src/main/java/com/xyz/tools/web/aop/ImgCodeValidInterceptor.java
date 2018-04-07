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
 * 根据imgKey或者当前sessionId来验证imgCode是否正确
 * 
 * @author lsf
 *
 */
public class ImgCodeValidInterceptor extends AbstractInterceptor implements ApplicationContextAware {

	private UserLoginHelper userLoginHelper;

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String imgcode = request.getParameter("imgCode");
		// String imgKey = request.getParameter("imgKey");
		if (StringUtils.isBlank(imgcode)) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "图片验证码不能为空");
		}
		boolean result = userLoginHelper.verifyImgCode(request, "r" + imgcode);
		if (!result) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "图片验证码不正确");
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
