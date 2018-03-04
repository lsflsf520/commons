package com.xyz.tools.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class FeatureUtil {

	public final static String DEFAULT_VAL_PREFIX = "val_";
	
	public final static String KEY_VAL_SPLITER = ";";
	
	/**
	 * 
	 * @param feature
	 * @return
	 */
	public static Map<String, String> parse2Map(String feature){
		Map<String, String> keyValMap = new HashMap<String, String>();
		
		int index = 0;
		if(!StringUtils.isBlank(feature)){
			String[] keyvalPairs = feature.split(KEY_VAL_SPLITER);
			for(String keyvalPair : keyvalPairs){
				String[] keyvalParts = keyvalPair.split("=");
				if(keyvalParts != null && keyvalParts.length == 2){
					keyValMap.put(keyvalParts[0], keyvalParts[1]);
				}else{
					keyValMap.put(DEFAULT_VAL_PREFIX + index++, keyvalPair);
				}
			}
		}
		
		return keyValMap;
	}
	
}
