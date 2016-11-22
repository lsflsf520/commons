package com.yisi.stiku.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.web.constant.WebConstant;

/**
 * 
 * @author shangfeng
 *
 */
public class LoginUtils {

	private final static Logger LOG = LoggerFactory.getLogger(LoginUtils.class);

	public static String getLogonRedirectUrl(HttpServletRequest request, String userTypeProjectName, List<Long> schoolIds) {

		String redirectUrl = WebConstant.getLogonUrl(userTypeProjectName, schoolIds);

		// String referer = request.getParameter("referer");
		// if(StringUtils.isNotBlank(referer)){
		// referer = urlDecode(referer);
		// if((referer.startsWith(WebConstant.getWebAppDomain(userType)) ||
		// referer.contains("/priv/info/")) && !referer.contains("/user/") &&
		// !referer.contains("/error/") && !(referer.endsWith("/web-student") ||
		// referer.endsWith("/web-student/")) &&
		// (referer.contains("17daxue.com") ||
		// referer.contains("51zhenduan.com"))){
		// redirectUrl = referer;
		// }
		// }

		return redirectUrl;
	}

	public static String urlDecode(String str) {

		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.warn(e.getMessage());
		}

		return str;
	}

	/**
	 * 
	 * @param userType
	 *            用户类型
	 * @return 根据指定的用户类型，返回一个标识该用户类型的字符串，主要用于从zk上获取对应的参数
	 */
	// public static String getUserTypeStr(int userType){
	// if(UserInfoUtil.isStudent(userType)){
	// return "student";
	// }else if(UserInfoUtil.isTeacher(userType)){
	// return "teacher";
	// }else if(UserInfoUtil.isJiaoYan(userType)){
	// return "jiaoyan";
	// }
	//
	// return "ms";
	// }
}
