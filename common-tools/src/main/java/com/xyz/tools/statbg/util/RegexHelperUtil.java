package com.xyz.tools.statbg.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.statbg.FlowData;
import com.xyz.tools.statbg.func.FuncContext;

/**
 * 
 * @author lsf
 * 
 */
public class RegexHelperUtil {

	/**
	 * 
	 * @return
	 */
	public static List<String> getParamNames(String str) {

		Pattern pattern = Pattern.compile("\\$\\{\\s*([^\\{\\}]+)\\s*\\}");
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

	public static Map<String, FuncContext> getKey2FuncMap(String str) {

		Pattern pattern = Pattern.compile("\\$(\\w+)\\(\\s*\\$\\{\\s*([^\\{\\}]+)\\s*\\}\\s*(,[^\\)]*\\s*)?\\)");
		Map<String, FuncContext> key2FuncMap = new HashMap<String, FuncContext>();

		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String funcName = matcher.group(1);
			String paramName = matcher.group(2);

			if (StringUtils.isNotBlank(paramName) && StringUtils.isNotBlank(funcName)) {
				String funcParamStr = matcher.group(3);
				FuncContext context = new FuncContext(funcName);
				if (StringUtils.isNotBlank(funcParamStr)) {
					String[] kvArr = funcParamStr.split(",");
					int index = 0;
					for (String kv : kvArr) {
						if (StringUtils.isNotBlank(kv)) {
							kv = kv.trim().replaceAll("'", "").replaceAll("\"", "");
							if (kv.contains(":")) {
								String[] kvparts = kv.split(":");
								if (kvparts.length != 2 || StringUtils.isBlank(kvparts[1])) {
									continue;
								}

								if (StringUtils.isBlank(kvparts[0])) {
									context.putKV(index + "", kvparts[1].trim());
								} else {
									context.putKV(kvparts[0].trim(), kvparts[1].trim());
								}
							}
						}
						++index;
					}
				}
				key2FuncMap.put(paramName.trim(), context);
			}
		}

		return key2FuncMap;
	}

	/**
	 * 
	 * @param originStr
	 * @param paramMap
	 * @return
	 */
	public static String replaceParams(String originStr, Map<String, List<FlowData>> paramMap) {

		List<String> paramNames = getParamNames(originStr);
		if (paramNames != null && paramNames.size() > 0) {
			String currFileNameFilter = originStr;

			for (String paramName : paramNames) {
				Object value = ParamFindUtil.getValue(paramName, paramMap, -1, true);

				currFileNameFilter = replaceParamName(currFileNameFilter, paramName,
						value == null ? paramName : value.toString());
			}

			originStr = currFileNameFilter;
		}

		return originStr;
	}

	/**
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

	public static String replaceParamName(String str, String paramName,
			Serializable value) {

		String val = value == null ? "null" : value.toString();
		return str.replaceAll("\\$\\w+\\(\\s*\\$\\{\\s*" + paramName + "\\s*\\}\\s*\\)",
				val).replaceAll("\\$\\{\\s*" + paramName + "\\s*\\}", val);
	}

}
