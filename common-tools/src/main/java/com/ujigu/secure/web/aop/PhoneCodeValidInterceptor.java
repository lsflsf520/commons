package com.ujigu.secure.web.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.web.filter.AbstractInterceptor;
import com.ujigu.secure.web.util.UserLoginUtil;

/**
 * 根据当前sessionId来校验用户输入的手机验证码 _code_ 是否正确
 * @author lsf
 *
 */
public class PhoneCodeValidInterceptor extends AbstractInterceptor{

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String code = request.getParameter("_code_");
//		String imgKey = request.getParameter("imgKey");
		if(StringUtils.isBlank(code)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "手机验证码不能为空");
		}
		String mobileOrEmail = request.getParameter("phone");
		if(StringUtils.isBlank(mobileOrEmail)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "用于校验的手机号不能为空");
		}
		
		
		boolean result = UserLoginUtil.validateCode(request, code, mobileOrEmail);
		if(!result){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "手机验证码不正确");
		}
		return result;
	}
	

}
