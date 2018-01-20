package com.ujigu.secure.web.common.ftl;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.web.util.UserLoginUtil;
import com.ujigu.secure.web.util.WebUtils;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Component("RespEncoder")
public class RespEncoder implements TemplateMethodModelEx{

	@Override
	@SuppressWarnings("all")
	public Object exec(List arguments) throws TemplateModelException {
		if(CollectionUtils.isEmpty(arguments) ){
			return "";
		}
		
		String url = arguments.get(0).toString();
//		url = addMyCode(url);
		
		HttpServletResponse response = ThreadUtil.get(ThreadUtil.RESPONSE_NAME);
		if(response == null){
			return url;
		}
		return response.encodeURL(url);
	}
	
}
