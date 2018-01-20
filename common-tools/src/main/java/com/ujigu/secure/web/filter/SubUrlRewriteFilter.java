package com.ujigu.secure.web.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.web.util.WebUtils;

public class SubUrlRewriteFilter extends UrlRewriteFilter {
	
	private final Set<String> extendNames = new HashSet<String>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		extendNames.add(".js");
		extendNames.add(".css");
		extendNames.add(".jpg");
		extendNames.add(".jpeg");
		extendNames.add(".png");
		extendNames.add(".swf");
		extendNames.add(".gif");
		extendNames.add(".ico");

		String excludeSuffixStr = filterConfig.getInitParameter("excludeSuffix");
		// excludeSuffix 的格式为以逗号分隔的文件后缀
		if (StringUtils.isNotBlank(excludeSuffixStr)) {
			String[] suffixes = excludeSuffixStr.split(",");
			for (String suffix : suffixes) {
				extendNames.add(suffix.startsWith(".") ? suffix : "." + suffix);
			}
		}
		
		super.init(filterConfig);
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String servletUri = httpRequest.getServletPath();
		if (!WebUtils.isStaticRes(servletUri, extendNames)) {
			ThreadUtil.put(ThreadUtil.CURR_URL, WebUtils.getCurrParamUrl(httpRequest));
		}
		
		super.doFilter(request, response, chain);
	}

}
