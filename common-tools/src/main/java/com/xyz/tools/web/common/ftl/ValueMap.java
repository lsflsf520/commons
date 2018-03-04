package com.xyz.tools.web.common.ftl;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.xyz.tools.common.constant.GlobalConstant;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.web.common.service.DataDictService;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Component("ValueMap")
public class ValueMap implements TemplateMethodModelEx{
	
	@Resource
	private DataDictService dataDictService;

	@Override
	public Object exec(List args) throws TemplateModelException {
		if(CollectionUtils.isEmpty(args) || args.size() < 1){
			throw new BaseRuntimeException("ILLEGAL_ARG", "namespace cannot be null");
		}
		String namespace = args.get(0).toString();
		if(args.size() == 1){
			return dataDictService.getKVMap(namespace);
		}
		String suffix = "";
		for(int i = 1; i < args.size(); i++){
			suffix += GlobalConstant.SQL_FIELD_SPLITER + args.get(i).toString();
		}
		
		Map<String, Serializable> kvMap = dataDictService.getKVMap(namespace);
		if(kvMap != null){
			Map<String, Serializable> filteredMap = new LinkedHashMap<String, Serializable>();
			for(String k : kvMap.keySet()){
				if(k.endsWith(suffix)){
					filteredMap.put(k.replace(suffix, ""), kvMap.get(k));
				}
			}
			
			return filteredMap;
		}
		
		return Collections.emptyMap();
	}

}
