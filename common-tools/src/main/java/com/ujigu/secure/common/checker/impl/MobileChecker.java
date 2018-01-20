package com.ujigu.secure.common.checker.impl;

import java.io.Serializable;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ujigu.secure.common.checker.Checker;
import com.ujigu.secure.common.checker.annotation.Validation;
import com.ujigu.secure.common.utils.RegexUtil;

@Component
public class MobileChecker implements Checker {

	@Override
	public boolean checkValue(Serializable value, Serializable regular, Validation validation,
			Map<String, Serializable> checkFieldMap) {
		return regular == null || !(Boolean)regular || RegexUtil.isPhone(value.toString());
	}

}
