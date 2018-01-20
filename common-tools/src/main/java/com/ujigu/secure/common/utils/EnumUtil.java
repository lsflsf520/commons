package com.ujigu.secure.common.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.exception.BaseRuntimeException;

public class EnumUtil {
	
	/**
	 * 
	 * @param clazz 枚举类型
	 * @param str 以英文逗号分隔枚举字符串
	 * @return 返回具体枚举类型的一个Set集合
	 */
	public static <T extends Enum<T>> Set<T> valueof(Class<T> clazz, String str){
		if(clazz == null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "参数clazz不能为空");
		}
		Set<T> enums = new LinkedHashSet<>();
		if(StringUtils.isBlank(str) ){
			return enums;
		}
		
		String[] parts = str.split(",");
		for(String part : parts){
			T t = Enum.valueOf(clazz, part.trim());
			enums.add(t);
		}
		
		return enums;
	}
	
	/**
	 * 
	 * @param enums
	 * @return 将enums集合以英文逗号为分隔符，组装成一个字符串返回
	 */
	public static <T extends Enum<T>> String toStr(Set<T> enums){
		if(CollectionUtils.isEmpty(enums)){
			return "";
		}
		
		return StringUtils.join(enums, ",");
	}
	
	/**
	 * 判断枚举enums中是否包含t
	 * @param enums
	 * @param t
	 * @return
	 */
	public static <T extends Enum<T>> boolean contains(Set<T> enums, T t){
		if(t == null || CollectionUtils.isEmpty(enums)){
			return false;
		}
		
		return enums.contains(t);
	}
	
	
}
