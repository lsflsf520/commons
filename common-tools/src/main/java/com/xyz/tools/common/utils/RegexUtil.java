package com.xyz.tools.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

public class RegexUtil {

	// private final static Pattern FLOAT_PATTERN =
	// Pattern.compile("^[+|-]?(\\d+\\.)?\\d+$");
	// private final static Pattern INT_PATTERN =
	// Pattern.compile("^[+|-]?\\d+$");
	// private final static Pattern EMAIL_PATTERN =
	// Pattern.compile("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$");
	// private final static Pattern MOBILE_PATTERN =
	// Pattern.compile("^(13|14|15|18)[0-9]{9}$") ;
	// private final static Pattern CNCODE_PATTERN =
	// Pattern.compile("[\u4e00-\u9fa5]") ;
	// private final static Pattern CHINESE_PATTERN =
	// Pattern.compile("^[\u4e00-\u9fa5]+$") ;
	private final static Pattern SPECIAL_PATTERN = Pattern
			.compile("[`~!@#$%^&*()+=|{}':;',//[//]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]");
	
	private final static String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i"    
            +"|windows (phone|ce)|blackberry"    
            +"|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp"    
            +"|laystation portable)|nokia|fennec|htc[-_]"    
            +"|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";    
	private final static String tableReg = "\\b(ipad|tablet|(Nexus 7)|up.browser"    
            +"|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";    
      
    //移动设备正则匹配：手机端、平板  
	private final static Pattern phonePat = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);    
	private final static Pattern tablePat = Pattern.compile(tableReg, Pattern.CASE_INSENSITIVE);    
        
    /** 
     * 检测是否是移动设备访问 
     *  
     * @Title: check 
     * @Date : 2014-7-7 下午01:29:07 
     * @param userAgent 浏览器标识 
     * @return true:移动设备接入，false:pc端接入 
     */  
    public static boolean isMobileClient(String userAgent){    
        if(StringUtils.isNotBlank(userAgent)){    
        	// 匹配    
        	Matcher matcherPhone = phonePat.matcher(userAgent);    
        	Matcher matcherTable = tablePat.matcher(userAgent);    
        	if(matcherPhone.find() || matcherTable.find()){    
        		return true;    
        	} 
        }    
        
        return false;    
    }  
    
    /**
     * 
     * @param regex 正则表达式
     * @param value 被检测的字符串
     * @return 如果value满足正则表达式regex的要求，则返回true；否则返回false
     */
    public static boolean checkRegex(String regex, String value){
    	if(StringUtils.isBlank(regex) || StringUtils.isBlank(value)){
    		return false;
    	}
    	
    	return value.matches(regex);
    }

	/**
	 * 
	 * <p>
	 * Title:isDecimal
	 * </p>
	 * <p>
	 * Description:this is a common method
	 * </p>
	 * 
	 * @param value
	 *            需要被验证的字符串
	 * @return 字符串如果为小数格式，则返回true； 否则返回false
	 */
	public static boolean isFloat(String value) {

		// Matcher matcher = FLOAT_PATTERN.matcher(value);
		// return matcher.matches();
		return StringUtils.isNotBlank(value) && value.matches("^[+|-]?(\\d+\\.)?\\d+$");
	}
	
	/**
	 * @param value 
	 * @return 是否是以0结尾的float 
	 */
	public static boolean isZeroEndFloat(String value) {

		return StringUtils.isNotBlank(value) && value.matches("^[+|-]?\\d+\\.0+$");
	}

	/**
	 * 
	 * @param value
	 *            需要被验证的字符串
	 * @return 字符串如果为整数格式，则返回true； 否则返回false
	 */
	public static boolean isInt(String value) {

		// Matcher matcher = INT_PATTERN.matcher(value);
		// return matcher.matches();
		return StringUtils.isNotBlank(value) && value.matches("^[+|-]?\\d+$");
	}

	public static boolean isUnsignedInt(String value) {

		return StringUtils.isNotBlank(value) && value.matches("^[+]?\\d+$");
	}

	/**
	 * 
	 * @return 是否是一个开闭区间的表达式
	 */
	public static boolean isOCExpression(String value) {

		return StringUtils.isNotBlank(value)
				&& (value.matches("^[\\(|\\[]\\s*\\d*\\s*,\\s*\\d+\\s*[\\)|\\]]$") || value
						.matches("^[\\(|\\[]\\s*\\d+\\s*,\\s*\\d*\\s*[\\)|\\]]$"));
	}

	public static boolean isEnglishOrDigital(String value) {

		return StringUtils.isNotBlank(value) && value.matches("^[A-Za-z0-9]+$");
	}

	/**
	 * 
	 * <Description>验证字符串value是否为邮箱格式</Description>
	 *
	 * @param value
	 *            需要被验证的字符串
	 * @return 字符串如果为邮箱格式，则返回true； 否则返回false
	 */
	public static boolean isEmail(String value) {

		// Matcher matcher = EMAIL_PATTERN.matcher(value);
		// return matcher.matches();
		return StringUtils.isNotBlank(value)
				&& value.matches("^([a-zA-Z0-9_-])+([\\.a-zA-Z0-9_-])*@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$");
	}

	/**
	 * 验证手机号格式
	 * 
	 * @param phone
	 *            需要被验证的字符串
	 * @return 字符串如果为手机号格式，则返回true；否则返回false
	 */
	public static boolean isPhone(String value) {

		// boolean flag = false;
		// try {
		// Matcher m = MOBILE_PATTERN.matcher(phone);
		// flag = m.matches();
		// } catch (Exception e) {
		// flag = false;
		// }
		// return flag;
		return StringUtils.isNotBlank(value) && value.matches("^(13|14|15|18|17)[0-9]{9}$");
	}
	
	/**
	 * 是否为固定电话号码
	 * @param value
	 * @return
	 */
	public static boolean isFixPhoneNum(String value){
		
		return StringUtils.isNotBlank(value) && value.matches("^(\\(?\\d{3,4}\\)?\\s*-?\\s*)?\\d{7,14}$");
	}
	
	/**
	 * 是否为身份证号码
	 * @param value
	 * @return
	 */
	public static boolean isIdentityNum(String value){
		
		return StringUtils.isNotBlank(value)
				     && (value.matches("^\\d{8}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$")  //15位身份证号码
				     || value.matches("^\\d{6}((19)|(2\\d))\\d{2}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}[0-9Xx]$"));
	}
	
	/**
	 * 判断字符串中是否包含中文
	 * 
	 * @param value
	 *            需要被验证的字符串
	 * @return 参数value中如果包含中文字符，则返回true；否则返回false
	 */
	public static boolean isContainChinese(String value) {

		// Matcher matcher = CNCODE_PATTERN.matcher(str);
		// boolean flg = false;
		// if (matcher.find()){
		// flg = true;
		// }
		// return flg;

		return StringUtils.isNotBlank(value) && value.matches(".*[\u4e00-\u9fa5].*");
	}

	/**
	 * 判断字符串是否完全由中文构成
	 * 
	 * @param value
	 *            需要被验证的字符串
	 * @return 如果参数value中的字符全部为中文，则返回true；否则返回false
	 */
	public static boolean isChinese(String value) {

		// Matcher matcher = CHINESE_PATTERN.matcher(str);
		// boolean flg = false;
		// if (matcher.find()){
		// flg = true;
		// }
		// return flg;

		return StringUtils.isNotBlank(value) && value.matches("^[\u4e00-\u9fa5]+$");
	}

	/**
	 * 
	 * @param dateStr
	 *            需要被验证的字符串
	 * @return 如果参数dateStr为yyyy-MM-dd的日期格式，则返回true；否则返回false
	 */
	public static boolean isDate(String dateStr) {

		return StringUtils.isNotBlank(dateStr) && dateStr.matches("\\d{4}-\\d{2}-\\d{2}");
	}

	/**
	 * 
	 * @param dateTimeStr
	 *            需要被验证的字符串
	 * @return 如果参数dateTimeStr为yyyy-MM-dd_HH:mm:ss或yyyy-MM-dd
	 *         HH:mm:ss的格式，则返回true；否则返回false
	 */
	public static boolean isDateTime(String dateTimeStr) {

		return StringUtils.isNotBlank(dateTimeStr)
				&& dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}[_|\\s]\\d{1,2}:\\d{1,2}:\\d{1,2}");
	}

	/**
	 * 过滤所有特殊字符
	 * 
	 * @param str
	 *            需要被过滤的字符串
	 * @return 将参数str中包含的SPECIAL_PATTERN指定的特殊字符，全部替换为空字符""，然后返回
	 * @throws PatternSyntaxException
	 */
	public static String specialFilter(String str) throws PatternSyntaxException {

		Matcher m = SPECIAL_PATTERN.matcher(str);
		return m.replaceAll("").trim();
	}

	public static boolean IsUrl(String url) {

		// String regex =
		// "(file|gopher|news|nntp|telnet|http|ftp|https|ftps|sftp):///?([\\w-]+\\.)+[\\w-]+(/.*)?";

		String regex = "^(((file|gopher|news|nntp|telnet|http|ftp|https|ftps|sftp)://)|(www\\.))+(([/|\\\\]?[a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))([/|\\\\].*)?$";

		return Pattern.matches(regex, url);
	}

	/**
	 * 
	 * @param content
	 *            包含数字的字符串
	 * @return 截取字符串中从左至右出现的第一串连续的数字字符串，并将其返回
	 */
	public static String getNumbers(String content) {

		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}

	/**
	 * 根据正则表达式的组规则抓取对应的value 例如：str =
	 * "busi_branch=chapter_test, to_resolve_branch=stiku-tmp_develop_chapter_test_CFCL"
	 * regex = "busi_branch=(.*), to_resolve_branch=(.*)" 返回： [chapter_test,
	 * stiku-tmp_develop_chapter_test_CFCL]
	 * 
	 * @param regex
	 * @param str
	 * @return
	 */
	public static List<String> extractGroups(String regex, String str) {

		Pattern pattern = Pattern.compile(regex);

		return extractGroups(pattern, str);
	}
	
	/**
	 * 根据正则regex从str中循环匹配，抓取对应的数据
	 * @return
	 */
	public static List<String> extractAll(String regex, String str){
		Pattern  pattern=Pattern.compile(regex);  
        Matcher  matcher=pattern.matcher(str);  
   
        List<String> valueList = new ArrayList<>();
        while(matcher.find()){  
        	String value = matcher.group();
        	
        	valueList.add(value);
        }
        
        return valueList;
	}

	/**
	 * 根据正则表达式的组规则抓取对应的value 例如：str =
	 * "busi_branch=chapter_test, to_resolve_branch=stiku-tmp_develop_chapter_test_CFCL"
	 * regex = "busi_branch=(.*), to_resolve_branch=(.*)" 返回： [chapter_test,
	 * stiku-tmp_develop_chapter_test_CFCL]
	 * 
	 * @param pattern
	 * @param str
	 * @return
	 */
	public static List<String> extractGroups(Pattern pattern, String str) {

		List<String> groupValues = new ArrayList<String>();

		Matcher matcher = pattern.matcher(str);

		if (matcher.find()) {
			int groups = matcher.groupCount();
			for (int index = 1; index <= groups; index++) {
				String groupValue = matcher.group(index);

				groupValues.add(groupValue);
			}
		}

		return groupValues;
	}
	
	/**
	 * 
	 * @return
	 */
	public static List<String> getParamNames(String str) {

		Pattern pattern = Pattern.compile("\\{\\s*([^\\{\\}]+)\\s*\\}");
		List<String> paramNames = new ArrayList<String>();

		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String paramName = matcher.group(1);

			if (StringUtils.isNotBlank(paramName)) {
				paramNames.add(paramName.trim());
			}
		}

		return paramNames;
	}

	public static String replaceParamName(String str, String paramName,
			Object value) {

		String val = value == null ? "null" : value.toString();
		return str.replaceAll("\\{\\s*" + paramName + "\\s*\\}", val);
	}

}
