package com.xyz.tools.web.aop;

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.xyz.tools.web.constant.WebConstant;

abstract public class AbstractInterceptor extends HandlerInterceptorAdapter {
	
	protected AntPathMatcher urlMatcher = new AntPathMatcher();

	protected Set<String> excludeUrls = new TreeSet<String>();

	{
		excludeUrls.add("/static/**");
		excludeUrls.add("/images/**");
		excludeUrls.add("/js/**");
		excludeUrls.add("/css/**");
		excludeUrls.add("/img/**");
	}
	
	abstract protected boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler, String requestUri) throws Exception;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		String requestUri = request.getServletPath();
		// 如果为 /ping/pang 链接，则直接返回true，不管当前用户有没有登录
		if (WebConstant.PING_PANG_URI.equals(requestUri)) {
			return true;
		}

		if (excludeUrls != null && !excludeUrls.isEmpty()) {
			for (String excludeUri : excludeUrls) {
				if (urlMatcher.match(excludeUri, requestUri)) {
					return true;
				}
			}
		}
		
		return preHandle(request, response, handler, requestUri);
	}
	
	public void setExcludeUrls(Set<String> excludeUrls) {

		if (excludeUrls != null && !excludeUrls.isEmpty()) {
			excludeUrls.addAll(this.excludeUrls);
			this.excludeUrls.clear();
			this.excludeUrls.addAll(excludeUrls);
		}

	}

}
