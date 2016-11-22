package com.yisi.stiku.common.checker.impl;

import java.io.Serializable;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yisi.stiku.common.checker.Checker;
import com.yisi.stiku.common.checker.annotation.Validation;
import com.yisi.stiku.common.utils.RegexUtil;

@Component
public class MobileChecker implements Checker {

	@Override
	public boolean checkValue(Serializable value, Serializable regular, Validation validation,
			Map<String, Serializable> checkFieldMap) {
		return regular == null || !(Boolean)regular || RegexUtil.isPhone(value.toString());
	}

}
