package com.yisi.stiku.common.utils;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.bean.UserType;


public class UserInfoUtil {

	public static boolean isTeacher(Integer userType){
    	return isUserType(userType, UserType.TEACHER);
    }
    
    public static boolean isStudent(Integer userType){
    	return userType != null && (UserType.STUDENT == userType || UserType.REG_STUDENT == userType || UserType.ZIZHU_PRINT_STUDENT == userType);
    }
    
    /**
     * 是否为学生体验账号
     * @param signName
     * @return
     */
    public static boolean isTYStudent(String signName){
    	return StringUtils.isNotBlank(signName) && (signName.startsWith("TY") || signName.startsWith("T18") || signName.startsWith("TA"));
    }
    
    public static boolean isJiaoYan(Integer userType){
    	return userType != null && userType == UserType.JIAOYAN;
    }
    
    public static boolean isAgent(Integer userType){
    	return isUserType(userType, UserType.AGENT);
    }
    
    public static boolean isSuperAdmin(Integer userType){
    	return isUserType(userType, UserType.SUPER_ADMIN);
    }
    
    public static boolean isCoach(Integer userType){
    	return isUserType(userType, UserType.COACH);
    }
    
    /**
     * @param checkUserType 需要被检查的用户类型
     * @param userType @see com.yisi.stiku.basedata.rpc.constant.UserType
     * @return 判断用户类型是否是指定的userType
     */
    public static boolean isUserType(Integer checkUserType, int userType){
    	return checkUserType != null && userType == checkUserType;
    }
	
}
