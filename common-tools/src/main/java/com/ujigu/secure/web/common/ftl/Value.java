package com.ujigu.secure.web.common.ftl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.web.common.service.DataDictService;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Component("Value")
public class Value implements TemplateMethodModelEx{
	
	@Resource
	private DataDictService dataDictService;

	@Override
	public Object exec(List args) throws TemplateModelException {
		if(CollectionUtils.isEmpty(args) || args.size() < 2){
			throw new BaseRuntimeException("ILLEGAL_ARG", "namespace and key cannot be null");
		}
		String namespace = args.get(0).toString();
		String key = "";
		if(args.size() >= 2){
			String[] currArgs = new String[args.size() - 1];
			for(int i = 1; i < args.size(); i++){
				currArgs[i - 1] = args.get(i) == null ? "" : args.get(i).toString();
			}
			key = dataDictService.buildKey(currArgs);
		}
		
		return dataDictService.getValue(namespace, key);
	}
	

}
