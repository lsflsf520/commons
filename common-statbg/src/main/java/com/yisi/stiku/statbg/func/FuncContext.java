package com.yisi.stiku.statbg.func;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.yisi.stiku.common.exception.BaseRuntimeException;

/**
 * @author shangfeng
 *
 */
public class FuncContext {

	private final Map<String, String> paramMap = new HashMap<String, String>();
	private final Func<?> func;

	private final static Map<String, Func<?>> funcMap = new HashMap<String, Func<?>>();

	static {
		funcMap.put("urldecode", new URLDecoder());
	}

	/**
	 * 
	 */
	public FuncContext(String funcName) {

		this.func = funcMap.get(funcName);
		if (func == null) {
			throw new BaseRuntimeException("NOT_SUPPORT", "there not found any Func instance for funcName '" + funcName
					+ "'");
		}
	}

	public void putKV(String key, String val) {

		paramMap.put(key, val);
	}

	public Map<String, String> getParamMap() {

		return Collections.unmodifiableMap(paramMap);
	}

	public Func<?> getFunc() {

		return func;
	}

	public static void registerFunc(String funcName, Func<?> func) {

		if (funcMap.containsKey(funcName)) {
			throw new BaseRuntimeException("ALREADY_EXIST", "funcName '" + funcName + "' already exists.");
		}
		funcMap.put(funcName, func);
	}

}
