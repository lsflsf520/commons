package com.xyz.tools.web.util;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.reflect.TypeToken;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.ThreadUtil;

public class WebParamUtils {
	
	/*public static int getIntParam(HttpServletRequest request, String paramName){
		String value = request.getParameter(paramName);
		
		return RegexUtil.isInt(value) ? Integer.valueOf(value) : 0;
	}

	public static String getParam(HttpServletRequest request, String paramName){
		
		return request.getParameter(paramName);
	}*/
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Type clazz){
		String content = ThreadUtil.getReqContent();
		
		return (T)JsonUtil.create().fromJson(content, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getParamMap(){
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		return (Map<String, Object>)getBean(type, type, new JsonUtil.MapTypeAdapter());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Type clazz, Type tmpType, Object typeAdapter){
		String content = ThreadUtil.getReqContent();
		if(StringUtils.isBlank(content)){
			return null;
		}
		return (T)JsonUtil.createTmpGson(tmpType, typeAdapter).fromJson(content, clazz);
	}
	
	public static Integer getInt(Map<String, Object> paramMap, String param) {

		try {
			Object value = paramMap.get(param);
			return value == null ? null : Double.valueOf(value.toString()).intValue();
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Double getDouble(Map<String, Object> paramMap, String param){
		try {
			Object value = paramMap.get(param);
			return value == null ? null : Double.parseDouble(value.toString());
		} catch (Exception e) {
		}
		return null;
	}

	public static Long getLong(Map<String, Object> paramMap, String param) {

		try {
			Object value = paramMap.get(param);
			return value == null ? null : Double.valueOf(value.toString()).longValue();
		} catch (Exception e) {
		}
		return null;
	}

	public static boolean getBoolean(Map<String, Object> paramMap, String param,
			boolean defaultValue) {

		try {
			Object value = paramMap.get(param);
			return value == null ? defaultValue : Boolean.parseBoolean(value.toString());
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static Float getFloat(Map<String, Object> paramMap, String param) {

		try {
			Object value = paramMap.get(param);
			return value == null ? null : Float.parseFloat(value.toString());
		} catch (Exception e) {
		}
		return null;
	}

	public static String getStr(Map<String, Object> paramMap, String param) {
		Object value = paramMap.get(param);
		
		return value == null ? null : value.toString().trim();
	}
	
}
