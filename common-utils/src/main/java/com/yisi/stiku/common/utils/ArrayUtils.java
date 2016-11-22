package com.yisi.stiku.common.utils;

import java.util.List;



/**
 * Created by Sean on 13-6-12.
 */
public class ArrayUtils {
	
	/**
	 * 判断list是否为null或empty
	 * 
	 * @param list
	 * @return
	 */
	public static boolean isEmpty(List list) {
		if (list == null)
			return true;
		return list.isEmpty();
	}
	

	/**
	 * 
	 * @param s 数字字符串数组
	 * @return 将String数组转换成int数组，并返回
	 */
    public static int[] stringToInts(String[] s) {
        int[] n = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            n[i] = Integer.parseInt(s[i]);
        }
        return n;
    }

    /**
     * 
     * @param s
     * @return 将String数组转换成Integer数组，并返回
     */
    public static Integer[] stringToIntegers(String[] s) {
        Integer[] n = new Integer[s.length];
        for (int i = 0; i < s.length; i++) {
            n[i] = Integer.parseInt(s[i]);
        }
        return n;
    }

    /**
     * 
     * @param separator 连接符
     * @param objects 数组
     * @return 把数组通过连接符拼接成字符串
     */
    public static String objs2StrLinkBy(String separator, Object... objects) {
        if (objects == null || objects.length == 0) return "";
        StringBuffer result = new StringBuffer();
        for (Object temp : objects) {
           if(temp != null)result.append(temp.toString()).append(separator);
        }
        if (result.toString().length() > 0) {
            return result.toString().substring(0, result.toString().lastIndexOf(separator));
        }
        return "";
    }
    
    /**
     * 
     * @param objArr Object对象数组
     * @return 将objArr转换成字符串数组返回
     */
    public static String[] objArr2StrArr(Object[] objArr){
    	if(objArr == null){
    		return null;
    	}
    	String[] values = new String[objArr.length];
    	int index = 0;
    	for(Object obj : objArr){
    		values[index++] = (obj == null ? null : obj.toString());
    	}
    	
    	return values;
    }
}
