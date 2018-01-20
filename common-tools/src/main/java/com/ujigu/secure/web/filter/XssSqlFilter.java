package com.ujigu.secure.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.ujigu.secure.web.filter.wrapper.XssSqlRequestWrapper;

public class XssSqlFilter implements Filter {
	
	@Override  
    public void destroy() {  
    }  
  
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		XssSqlRequestWrapper xssRequest = new XssSqlRequestWrapper((HttpServletRequest) request);  
		/*HttpServletRequest xssRequest = (HttpServletRequest) request;
		Map<String, String[]> paramMap = xssRequest.getParameterMap();
		for(String key : paramMap.keySet()){
			String[] values = paramMap.get(key);
			if(values != null){
				for(int i = 0; i < values.length; i++){
					values[i] = stripXSSAndSql(values[i]);
				}
//				paramMap.put(key, values);
			}
		}*/
        chain.doFilter(xssRequest, response); 
		
	}  
	
}
