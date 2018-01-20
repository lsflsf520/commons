package com.ujigu.secure.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * OMS系统自用的String工具类 88209759
 */
public class StringUtil {

	private final static Logger LOG = LoggerFactory.getLogger(StringUtil.class);
	
	/**
	 * 用来对手机或者邮箱号码进行隐藏
	 *
	 * @param str
	 *            手机号或邮箱号或固定电话
	 * @return 将手机号或邮箱号中间的部分字符替换为*号，用于在web端显示出来
	 */
	public static String stringHide(String str){
		
		return stringHide(str, null);
	}
	
	/**
	 * 用来对手机或者邮箱号码进行隐藏
	 *
	 * @param str
	 *            手机号或邮箱号
	 * @param prefix 需要去掉str参数中指定的前缀 prefix ,之后再对剩下的部分进行处理
	 * @return 将手机号或邮箱号中间的部分字符替换为*号，用于在web端显示出来
	 */
	public static String stringHide(String str, String prefix) {
		if(StringUtils.isBlank(str)){
			return str;
		}
		if(StringUtils.isNotBlank(prefix)){
			str = str.replaceFirst(prefix, "");
		}
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
			str = strBuilder.toString();
		} else if (RegexUtil.isPhone(str)) {
			int begin = 3;
			int end = 4;
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
			str = strBuilder.toString();
		} else if(RegexUtil.isFixPhoneNum(str)){
			int begin = 2;
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
			str = strBuilder.toString();
		}

        return StringUtils.isNotBlank(prefix) ? prefix + str : str;
	}
	
	/**
	 * 银行卡号脱敏
	 * @param bankCardNum
	 * @return
	 */
	public static String stringHideBankCardNum(String bankCardNum){
		if(StringUtils.isBlank(bankCardNum) || bankCardNum.length() < 7){
			return bankCardNum;
		}

        return bankCardNum.substring(0, 2) + "**" + bankCardNum.substring(bankCardNum.length() - 4);
	}
	
	/**
	 * 左填充0
	 *
	 * @param length
	 *            长度
	 * @param number
	 *            数字
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
	 * @param str
	 *            字符串
	 * @return 将字符串str的第一个字母变为小写之后返回
	 */
	public static String lowerFirst(String str) {
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	/**
	 * 
	 * @return 生成一个随机的uuid
	 */
	public static String genUuid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 
	 * @param regex
	 * @param content
	 * @param varGroupIndex
	 *            如果匹配到了regex，那么可以指定用regex正则表达式中的组的序号，来选择作为变量的内容返回 例如：regex 为
	 *            #(\\w+)# ， content 为 "sshwwd #var1# jsdf #var2# ouso #var3#
	 *            elliue" 如果 varGroupIndex = 0，那么方法应返回 #var1# 、#var2#、#var3#
	 *            这三个字符串构成的集合 如果 varGroupIndex = 1，那么方法应返回 var1 、var2、 var3
	 *            这三个字符串构成的集合 如果 varGroupIndex =
	 *            2，则会返回一个空的集合，因为regex中只有两个组(一对小括号为一组，regex本身是一个组)，序号从0开始
	 * @return 从字符串内容中查找出与regex规则匹配的变量
	 */
	public static Set<String> searchVar(String regex, String content, int varGroupIndex) {
		Set<String> varSet = new HashSet<String>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			if (varGroupIndex <= matcher.groupCount()) {
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

	
	/**
	 * 判断字符串str中是否有sql注入
	 * @param str
	 * @return
	 */
	private static final String[] INJ_STR_ARR = new String[]{"'", " and ", " exec ", " insert ", " select ", " delete ", " update ", " count ", "*", "%", " chr ", " mid ", " master ", " truncate ", " char ", " declare ", ";", " or ", "-", "+"};
	public static boolean hasSqlInject(String str) {
		if(StringUtils.isNotBlank(str)){
			for (int i = 0; i < INJ_STR_ARR.length; i++) {
				if (str.indexOf(INJ_STR_ARR[i]) >= 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String urlDecode(String str) {

		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.warn(e.getMessage());
		}

		return str;
	}

	public static String urlEncode(String str) {

		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.warn(e.getMessage());
		}

		return str;
	}
	
	/**过滤掉提交文本的标签字符 暂时去掉img及a标签
	 * 暂时正则处理img标签和a标签   以下处理还有问题  后续需进一步完善
	 * @return 
	 */
	public static String contentFilter(String content){
		if(StringUtils.isBlank(content)){
			return "";
		}
//		List<String> strs = RegexUtil.extractGroups(">([^</]+)</", content);
//		return content.replaceAll("<img[^>]*[/]?>", "").replaceAll("<a[^>]*>.*</a>", "");
		String[] parts = content.split("<\\w+\\s*[^>]*>");
		List<String> contentList = new ArrayList<>();
		for(String part : parts){
			String firstpart = part.split("</")[0].trim();
			if(StringUtils.isNotBlank(firstpart)){
				contentList.add(firstpart);
			}
		}
		return StringUtils.join(contentList, "，");
	}
	
	public static String escapeXss(String value){
		if(StringUtils.isBlank(value)){
			return "";
		}
		
		value = value.replaceAll("<(?i)script([^>]*)?>", "&lt;script&gt;").replaceAll("</(?i)script>", "&lt;/script&gt;");//替换script标签
		value = value.replaceAll("(?i)on[\\w]+=", ""); //替换事件绑定
		//TODO 还需要判断img的src内容是否合法，a标签中的href中的内容是否合法
		List<String> hrefs = RegexUtil.extractGroups("(?i)href=['\"]?([^'\\(\"]*)['\"]?", value);
		List<String> srcs = RegexUtil.extractGroups("(?i)src=['\"]?([^'\\(\"]*)['\"]?", value);
		hrefs.addAll(srcs);
		if(!CollectionUtils.isEmpty(hrefs)){
			for(String str : hrefs){
				if((!str.contains("/") && !str.contains("\\")) || str.contains("'") || str.contains("\"")){
					value = value.replace(str, "");
				}
			}
		}
		return value;
	}
	
	/** 分词
	 * @param content  分词的字符串
	 * @param regex 分词的正则 例如 (.{1})
	 * @param des 需要替换的分词  例如%
	 * @return
	 */
	public static String segment(String content,String regex,String des){
		if(StringUtils.isBlank(content)){
			return "";
		}
		content = content.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "").replaceAll("\\s*", "").replaceAll(" ", "");
		return content.replaceAll (regex, "$1"+des);
	}
	
	/**
	 * 以 逗号 作为分隔符，将str分割后，按照字符串中的顺序组装成有序的Set集合
	 * @param str
	 * @return
	 */
	public static Set<String> toSet(String str){
		
		return toSet(str, ",");
	}
	
	/**
	 * 按照指定的spliter分隔符，将str分割后，按照字符串中的顺序组装成有序的Set集合
	 * @param str 需要被转成Set集合的字符串
	 * @param spliter 字符串中的分隔符
	 * @return
	 */
	public static Set<String> toSet(String str, String spliter){
		Set<String> strs = new LinkedHashSet<>();
		if(StringUtils.isNotBlank(str)){
			String[] parts = str.split(spliter);
			for(String part : parts){
				strs.add(part);
			}
		}
		
		return strs;
	}
	
	/**
	 * @param str
	 * @param des
	 * @return
	 * @Decription 获取字符串中包含某个字符串的个数
	 * @Author Administrator
	 * @Time 2017年10月11日上午11:39:11
	 * @Exception
	 */
	public static int containNum(String str,String des){
		if(!str.contains(des)){
			return 0;
		}
		int num = 0;
		while(str.contains(des)){
			num++;
			str = str.replaceFirst(des, "");
		}
		return num;
	}
	
	public static String getDomainPrefix(String domain){
		String prefix = "";
		if(StringUtils.isNotBlank(domain)){
			String[] parts = domain.split("\\."); 
			if(parts.length <= 2){
				prefix = "www";
			} else {
				prefix = StringUtils.join(parts, ".", 0, parts.length - 2);
				if(prefix.startsWith("www.")){
					prefix = prefix.substring(4);
				}
				if(prefix.startsWith("m.")){
					prefix = prefix.substring(2);
				}
				if("m".equals(prefix)){
					prefix = "www";
				}
			}
		}
		return  prefix;
	}
	
	public static String getBxDomainPrefix(String domain){
		String prefix = "";
		if(StringUtils.isNotBlank(domain)){
			String[] parts = domain.split("\\."); 
			if(parts.length <= 2){
				prefix = "www";
			} else {
				prefix = StringUtils.join(parts, "-", 0, parts.length - 2);
				if(prefix.startsWith("www-")){
					prefix = prefix.substring(4);
				}
				if(prefix.startsWith("m-")){
					prefix = prefix.substring(2);
				}
				if("m".equals(prefix)){
					prefix = "www";
				}
			}
		}
		return  prefix;
	}
	
	public static String getOrderId(){
		return new SimpleDateFormat("yyMMddHHmmss").format(new Date())+(int)((Math.random()*9+1)*100000);
	}
}
