package com.xyz.tools.statbg.func;

import java.util.Map;

/**
 * @author shangfeng
 *
 */
public interface Func<T> {

	T convert(Object value, Map<String, String> funcParamMap);

}
