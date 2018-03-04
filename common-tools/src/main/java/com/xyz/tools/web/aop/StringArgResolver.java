package com.xyz.tools.web.aop;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.xyz.tools.common.utils.StringUtil;

public class StringArgResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return "java.lang.String".equals(parameter.getParameterType().getName());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String value = webRequest.getParameter(parameter.getParameterName());
		if(value != null){
			value = StringUtil.escapeXss(value);
		}
		return value;
	}

}
