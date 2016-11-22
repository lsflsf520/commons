package com.yisi.stiku.common.checker;

import java.io.Serializable;
import java.util.Map;

import com.yisi.stiku.common.checker.annotation.Validation;

/**
 * 
 * @author shangfeng
 *
 */
public interface Checker {

	/**
	 * 
	 * @param value 当前需要校验的value
	 * @param regular 校验规则的描述字符串
	 * @param checkFieldMap 对象中的需要进行校验的字段集合
	 * @return 校验通过返回true；否则返回false
	 */
	boolean checkValue(Serializable value, Serializable regular, Validation validation, Map<String/*field name*/, Serializable/*field value*/> checkFieldMap);
	
}
