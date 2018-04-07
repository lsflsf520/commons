package com.xyz.tools.web.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.web.common.service.CommonHeaderLoader.CommonHeader;
import com.xyz.tools.web.common.service.CommonHeaderService;

public class TKDInterceptor extends AbstractInterceptor {

	private CommonHeaderService commonHeaderService;

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

	private CommonHeader getHeader(HttpServletRequest request, ModelAndView mav) {
		CommonHeader header = commonHeaderService.getHeader(request, mav);

		String title = getSiteName();
		if (header != null && StringUtils.isNotBlank(header.getTitle())) {
			if (excludeUrls != null && !excludeUrls.isEmpty()) {
				for (String excludeUri : excludeUrls) {
					if (urlMatcher.match(excludeUri, request.getServletPath())) {
						return header;
					}
				}
			}

			title = title + "-" + header.getTitle();
		}
		if (header == null) {
			header = new CommonHeader(title, null, null);
		} else {
			header.setTitle(title);
		}

		return header;
	}

	protected String getSiteName() {
		return BaseConfig.getValue("site.name");
	}

	public void setCommonHeaderService(CommonHeaderService commonHeaderService) {
		this.commonHeaderService = commonHeaderService;
	}

}
