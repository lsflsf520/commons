package com.xyz.tools.web.filter.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.xyz.tools.common.utils.StringUtil;

/**
 * 参考：http://blog.csdn.net/hithedy/article/details/50630109
 * @author lsf
 *
 */
public class XssSqlRequestWrapper extends HttpServletRequestWrapper{
	
	HttpServletRequest orgRequest = null;

	public XssSqlRequestWrapper(HttpServletRequest request) {
		super(request);
		this.orgRequest = request;
	}

	/** 
     * 覆盖getParameter方法，将参数名和参数值都做xss & sql过滤。<br/> 
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取<br/> 
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖 
     */  
    @Override  
    public String getParameter(String name) {  
        String value = super.getParameter(stripXSSAndSql(name));  
        if (value != null) {  
            value = stripXSSAndSql(value);  
        }  
        return value;  
    }  
  
    /** 
     * 覆盖getHeader方法，将参数名和参数值都做xss & sql过滤。<br/> 
     * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/> 
     * getHeaderNames 也可能需要覆盖 
     */  
    @Override  
    public String getHeader(String name) {  
  
        String value = super.getHeader(stripXSSAndSql(name));  
        if (value != null) {  
            value = stripXSSAndSql(value);  
        }  
        return value;  
    } 
	
    private String stripXSSAndSql(String value){
    	return StringUtil.escapeXss(value);
    }
}
