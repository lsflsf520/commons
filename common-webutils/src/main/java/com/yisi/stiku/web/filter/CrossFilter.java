package com.yisi.stiku.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shangfeng
 *
 */
public class CrossFilter implements Filter {

	private final static Logger LOG = LoggerFactory.getLogger(CrossFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (response instanceof HttpServletResponse) {
			HttpServletResponse alteredResponse = ((HttpServletResponse) response);
			addHeadersFor200Response(alteredResponse, (HttpServletRequest) request);
		}
		chain.doFilter(request, response);
	}

	private void addHeadersFor200Response(HttpServletResponse response, HttpServletRequest request) {

		String origin = request.getHeader("Origin");
		LOG.debug(LOG.isDebugEnabled() ? "addHeadersFor200Response  head origin:" + origin : null);
		if (origin != null
				&& (origin.endsWith("17daxue.com") || origin.endsWith("17daxue.cn") || origin.endsWith("51zhenduan.com")))
		{
			response.addHeader("Access-Control-Allow-Origin",
					origin);
			response.setHeader("Access-Control-Allow-Headers",
					"Origin, X-Requested-With, Content-Type, Accept, Cookie");
			response.setHeader("Access-Control-Allow-Methods",
					"GET, POST, PUT, DELETE, OPTIONS");
			response.addHeader("Access-Control-Max-Age", "1728000");
			response.addHeader("Access-Control-Allow-Credentials", "true");
		}
	}

	@Override
	public void destroy() {

	}

}
