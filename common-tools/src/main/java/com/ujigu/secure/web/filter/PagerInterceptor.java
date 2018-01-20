package com.ujigu.secure.web.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.github.miemiedev.mybatis.paginator.domain.PageList;

public class PagerInterceptor extends HandlerInterceptorAdapter{

	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		Map<String, Object> tempPageMap = new HashMap<>();
		Enumeration<String> attrNames = request.getAttributeNames();
		if(attrNames != null){
			while(attrNames.hasMoreElements()){
				String attrName = attrNames.nextElement();
				Object val = request.getAttribute(attrName);
				if(val instanceof PageList){
					PageList pageList = (PageList)val;
					tempPageMap.put(attrName + "Pager", pageList.getPaginator());
				}
			}
		}
		
		if(modelAndView != null){
			Map<String, Object> modelMap = modelAndView.getModel();
			if(modelMap != null){
				for(String key : modelMap.keySet()){
					Object val = modelMap.get(key);
					if(val instanceof PageList){
						PageList pageList = (PageList)val;
						tempPageMap.put(key + "Pager", pageList.getPaginator());
					}
				}
			}
		}
		
		if(!CollectionUtils.isEmpty(tempPageMap)){
			for(String key : tempPageMap.keySet()){
				request.setAttribute(key, tempPageMap.get(key));
			}
		}
	}
	
}
