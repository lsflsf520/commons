package com.yisi.stiku.common.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OMS系统自用的String工具类 88209759
 */
public class StringUtil {

    /**
     * 用来对手机或者邮箱号码进行隐藏
     *
     * @param str 手机号或邮箱号
     * @return 将手机号或邮箱号中间的部分字符替换为*号，用于在web端显示出来
     */
    public static String stringHide(String str) {
        if (RegexUtil.isEmail(str)) {
            int index = str.indexOf('@');
            String prex = str.substring(0, index);
            String sufx = str.substring(index);
            StringBuilder strBuilder = new StringBuilder();
            if (prex.length() < 3) {
            	strBuilder.append(prex.charAt(0));
                strBuilder.append("***").append(sufx);
            } else {
                strBuilder.append(prex.charAt(0));
                for (int i = 1; i < prex.length() - 1; i++) {
                    strBuilder.append("*");
                }
                strBuilder.append(prex.charAt(prex.length() - 1));
                strBuilder.append(sufx);
            }
            return strBuilder.toString();
        } else if (RegexUtil.isPhone(str)) {
            int begin = 3;
            int end = 3;
            StringBuilder strBuilder = new StringBuilder();
            if (str.length() > 5) {
                for (int i = 0; i < begin; i++) {
                    strBuilder.append(str.charAt(i));
                }
                for (int i = begin; i < str.length() - end; i++) {
                    strBuilder.append("*");
                }
                for (int i = str.length() - end; i < str.length(); i++) {
                    strBuilder.append(str.charAt(i));
                }
            } else {
                for (int i = 0; i < str.length(); i++) {
                    if (i == 0 || i == str.length() - 1) {
                        strBuilder.append(str.charAt(i));
                    } else {
                        strBuilder.append("*");
                    }
                }
            }
            return strBuilder.toString();
        } else {
            return str;
        }

    }

    /**
     * 左填充0
     *
     * @param length 长度
     * @param number 数字
     * @return
     */
    public static String lpad(int length, int number) {
        if (length <= (number + "").length()) {
            return number + "";
        }
        String f = "%0" + length + "d";
        return String.format(f, number);
    }
    
    /**
     * 
     * @param str 字符串
     * @return 将字符串str的第一个字母变为小写之后返回
     */
    public static String lowerFirst(String str){
    	return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    /**
     * 
     * @return 生成一个随机的uuid
     */
    public static String genUuid(){
    	return UUID.randomUUID().toString();
    }
    
    /**
     * 
     * @param regex
     * @param content
     * @param varGroupIndex 如果匹配到了regex，那么可以指定用regex正则表达式中的组的序号，来选择作为变量的内容返回
     *                      例如：regex 为 #(\\w+)# ，
     *                            content 为 "sshwwd #var1# jsdf #var2# ouso #var3# elliue"
     *                            如果 varGroupIndex = 0，那么方法应返回 #var1# 、#var2#、#var3# 这三个字符串构成的集合
     *                            如果 varGroupIndex = 1，那么方法应返回 var1 、var2、 var3 这三个字符串构成的集合
     *                            如果 varGroupIndex = 2，则会返回一个空的集合，因为regex中只有两个组(一对小括号为一组，regex本身是一个组)，序号从0开始
     * @return 从字符串内容中查找出与regex规则匹配的变量
     */
    public static Set<String> searchVar(String regex, String content, int varGroupIndex){
    	Set<String> varSet = new HashSet<String>();
    	Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
        	if(varGroupIndex <= matcher.groupCount()){
        		varSet.add(matcher.group(varGroupIndex));
        	}
        }
        
        return varSet;
    }
    
    /**
	 * 判断一个字符串是否为null或空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * 判断一个字符串是否为null或空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotNull(String str) {
		return !isNull(str);
	}
}
