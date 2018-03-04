package com.xyz.tools.web.aop;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.RegexUtil;

/**
 * 根据当前sessionId来校验用户输入的手机验证码 _code_ 是否正确
 * @author lsf
 *
 */
public class PhoneRegExistCheckInterceptor extends AbstractInterceptor{
	
	@Resource
	private PhoneExistChecker phoneExistChecker;

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String needCheckExist = request.getParameter("needCheckExist");
		if(StringUtils.isBlank(needCheckExist) || !"true".equals(needCheckExist.trim())){
			return true;
		}
		
		String phone = request.getParameter("phone");
		if(phone == null || !RegexUtil.isPhone(phone = phone.trim())){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "手机号为空或格式不正确");
		}
		
		boolean exist = phoneExistChecker.isExist(phone);
		if(exist){
			throw new BaseRuntimeException("DATA_EXIST", "手机号已存在");
		}
		
		return true;
	}
	
	public void setPhoneExistChecker(PhoneExistChecker phoneExistChecker) {
		this.phoneExistChecker = phoneExistChecker;
	}

	public static interface PhoneExistChecker {
		
		boolean isExist(String phone);
		
	}
	
}
