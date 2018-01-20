package com.ujigu.secure.web.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.web.common.service.CommonHeaderService;
import com.ujigu.secure.web.common.service.CommonHeaderLoader.CommonHeader;
import com.ujigu.secure.web.filter.AbstractInterceptor;

public class TKDInterceptor extends AbstractInterceptor{

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		CommonHeader header = getHeader(request, modelAndView);
		request.setAttribute("tkd", header);
	}
	
	private CommonHeader getHeader(HttpServletRequest request, ModelAndView mav){
		CommonHeader header = CommonHeaderService.getHeader(request, mav);
		
		String title = getSiteName();
		if(header != null && StringUtils.isNotBlank(header.getTitle())){
			if (excludeUrls != null && !excludeUrls.isEmpty()) {
				for (String excludeUri : excludeUrls) {
					if (urlMatcher.match(excludeUri, request.getServletPath())) {
						return header;
					}
				}
			}
			
			title = title + "-" + header.getTitle();
		}
		if(header == null){
			header = new CommonHeader(title, null, null);
		} else {
			header.setTitle(title);
		}
		
		return header;
	}
	
	protected String getSiteName() {
		return BaseConfig.getValue("site.name");
	}
}
