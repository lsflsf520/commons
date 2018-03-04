package com.xyz.tools.common.utils;

public class DataConvertUtil {
	
	public static Integer parseInt(Object obj, Integer defval){
		String val = parseStr(obj, "");
		if(val.contains(".")){
			val = val.split("\\.")[0];
		}
		
		return RegexUtil.isInt(val) ? Integer.valueOf(val) : defval;
	}

	public static int parseInt(Object obj){
		return parseInt(obj, 0);
	}
	
	public static boolean parseBool(Object obj){
		String val = parseStr(obj, "false");
		
		return Boolean.valueOf(val);
	}
	
	public static String parseStr(Object obj){
		return parseStr(obj, "");
	}
	
	public static String parseStr(Object obj, String defval){
		if(obj == null) {
			return defval;
		}
		if(RegexUtil.isZeroEndFloat(obj.toString())) {
			return obj.toString().split("\\.")[0];
		}
		
		return obj.toString().trim();
	}
	
	
}
