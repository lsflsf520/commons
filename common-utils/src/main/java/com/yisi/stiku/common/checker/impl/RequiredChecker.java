package com.yisi.stiku.common.checker.impl;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.yisi.stiku.common.checker.Checker;
import com.yisi.stiku.common.checker.annotation.Validation;

/**
 * 
 * @author shangfeng
 *
 */
@Component
public class RequiredChecker implements Checker {

	@Override
	public boolean checkValue(Serializable value, Serializable regular, Validation validation,
			Map<String, Serializable> checkFieldMap) {
		if(regular == null || !(Boolean)regular){
			return true; //如果不需要验证，则直接返回true
		}
		return value != null && StringUtils.isNotBlank(value.toString());
	}

}
