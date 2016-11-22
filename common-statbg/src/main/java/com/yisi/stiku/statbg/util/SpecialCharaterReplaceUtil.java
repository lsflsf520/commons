package com.yisi.stiku.statbg.util;

/**
 * 
 * @author lsf
 *
 */
public class SpecialCharaterReplaceUtil {

	public static Object replace(Object val) {

		if (val instanceof String) {
			String valStr = (String) val;
			val = valStr.replaceAll("%40", "@");
		}

		return val;
	}

}
