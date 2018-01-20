package com.ujigu.secure.web.common.ftl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ujigu.secure.common.exception.BaseRuntimeException;

import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
@Component("ContainStr")
public class ContainStr implements TemplateMethodModelEx{

	@Override
	public Object exec(List args) throws TemplateModelException {
		if(args.size() < 2){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "参数个数至少为2个");
		}
		
		if(args.get(0) instanceof SimpleSequence && args.get(1) != null){
			SimpleSequence fmodel = (SimpleSequence)args.get(0);
			String smodel = args.get(1).toString();
			
			if(fmodel != null){
				for(Object obj : fmodel.toList()){
					if(obj != null && obj.toString().equals(smodel)){
						return true;
					}
				}
			}
		}
		
		return false;
	}

}
