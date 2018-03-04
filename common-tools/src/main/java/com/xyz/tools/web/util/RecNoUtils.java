package com.xyz.tools.web.util;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.utils.RegexUtil;
import com.xyz.tools.web.constant.ThirdChannel;

public class RecNoUtils {

	/**
	 * 
	 * @param seed
	 * @param firstCharInChannel 如果是第三方渠道注册的用户，需要加入该标志字符
	 * @return
	 */
	public static String buildUserRecNo(long seed, ThirdChannel thirdChannel){
		return "a" + (thirdChannel == null ? "" : thirdChannel.getShort()) + seed;
	}
	
	/**
	 * 直接从网站注册的用户推荐码生成规则
	 * @param seed
	 * @return
	 */
	public static String buildUserRecNo(long seed){
		return buildUserRecNo(seed, null);
	}
	
	public static String buildDepartRecNo(long seed){
		return "j" + seed;
	}
	
	public static boolean isThirdUser(String recNo){
		return StringUtils.isNotBlank(recNo) && recNo.length() > 2 && !RegexUtil.isInt(recNo.substring(1, 2));
	}
	
	/**
	 * 根据推荐码返回对应的第三方渠道的枚举类型，如果不是第三方渠道注册的用户，则返回null；
	 * @param recNo
	 * @return
	 */
	public static ThirdChannel parseThirdChannel(String recNo){
		String secondCh = null;
		if(StringUtils.isBlank(recNo) || RegexUtil.isInt(secondCh = recNo.substring(1, 2))){
			return null;
		}
		
		return ThirdChannel.getByShort(secondCh);
	}
	
	public static boolean isDepart(String recNo){
		return StringUtils.isNotBlank(recNo) && recNo.startsWith("j");
	}
	
	public static boolean isRegUser(String recNo){
		return StringUtils.isNotBlank(recNo) && recNo.startsWith("a");
	}
	
}
