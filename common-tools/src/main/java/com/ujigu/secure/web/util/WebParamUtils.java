package com.ujigu.secure.web.util;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.reflect.TypeToken;
import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.ThreadUtil;

public class WebParamUtils {
	
	/*public static int getIntParam(HttpServletRequest request, String paramName){
		String value = request.getParameter(paramName);
		
		return RegexUtil.isInt(value) ? Integer.valueOf(value) : 0;
	}

	public static String getParam(HttpServletRequest request, String paramName){
		
		return request.getParameter(paramName);
	}*/
	
	/**
	 * 根据站点ID返回资源对应的域名
	 * @param siteId
	 * @return
	 */
	public static String getLinkDomain(Integer siteId){
		if(siteId != null && siteId > 0){
    		return "";
    	}
    	
    	return (GlobalConstant.IS_WEB_PC || GlobalConstant.IS_WEB_H5 ?
    			    "http://" + BaseConfig.getValue(ThreadUtil.getViewPrefix() + ".main.domain", "www.xicaimao.cn") 
    			    : BaseConfig.getValue("pc.main.domain", "www.xicaimao.cn")
    			) + (80 == ThreadUtil.getCurrPort() ? "" : ":" + ThreadUtil.getCurrPort())
    			;
	}
	
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
