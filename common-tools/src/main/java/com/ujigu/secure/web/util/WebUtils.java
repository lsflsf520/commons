package com.ujigu.secure.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.Maps;
import com.google.gson.JsonSerializer;
import com.ujigu.secure.common.bean.ResultCodeIntf;
import com.ujigu.secure.common.strategy.ResultCodeSerializer;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.common.utils.ThreadUtil;

/**
 * Created by Sean on 13-6-8.
 */
public class WebUtils {

	public final static String PC = "pc";
	public final static String ANDROID = "ANDROID";
	public final static String IPHONE = "IPHONE";
	public final static String IPAD = "IPAD";
	public final static String WINDOWS_PHONE = "WINDOWS PHONE";
	public final static String IPOD = "IPOD";

	public static final String DEFAULT_JSON_FILTER_NAME = "s2jhFilter";

	private static final Logger LOG = LoggerFactory.getLogger(WebUtils.class);

	private static final SimpleFilterProvider serializeAllFilterProvider = new SimpleFilterProvider()
			.addFilter(DEFAULT_JSON_FILTER_NAME,
					SimpleBeanPropertyFilter.serializeAllExcept());

	private static final ThreadLocal<SimpleBeanPropertyFilter> simpleBeanPropertyFilterContainer = new ThreadLocal<SimpleBeanPropertyFilter>();

	public static int getParam(HttpServletRequest request, String param,
			int defaultValue) {

		try {
			String value = request.getParameter(param);
			return Integer.parseInt(value);
		} catch (Exception e) {
		}
		return defaultValue;
	}

	/*
	 * 从session取得值。
	 */
	public static Object getParamForSession(HttpServletRequest request,
			String param, Object defaultValue) {

		try {
			Object value = request.getSession().getAttribute(param);
			return value;
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static long getParam(HttpServletRequest request, String param,
			long defaultValue) {

		try {
			String value = request.getParameter(param);
			return Long.parseLong(value);
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static boolean getParam(HttpServletRequest request, String param,
			boolean defaultValue) {

		try {
			String value = request.getParameter(param);
			return Boolean.parseBoolean(value);
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static float getParam(HttpServletRequest request, String param,
			float defaultValue) {

		try {
			String value = request.getParameter(param);
			return Float.parseFloat(value);
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public static String getParam(HttpServletRequest request, String param,
			String defaultValue) {

		String value = request.getParameter(param);
		if (value != null) {
			value = value.trim();
		}
		return (StringUtils.isBlank(value)) ? defaultValue : value;
	}

	public static String getUTF8Value(HttpServletRequest request, String param) {

		String value = request.getParameter(param);

		return decodeParam(value, "UTF-8");
	}

	public static String decodeParam(String value, String charset) {

		try {
			if (StringUtils.isNotBlank(value)) {
				return new String(value.getBytes("ISO-8859-1"), charset);
			}
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}

		return null;
	}

	public static String getCookieValue(HttpServletRequest request,
			String cookieName) {

		Assert.notNull(request);
		Assert.hasText(cookieName);
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie != null && cookie.getName() != null
						&& cookie.getName().equals(cookieName)) {
					// 特殊字符串处理
					String rValue = decode(cookie.getValue());
					rValue = urlDecode(rValue);
					return rValue;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param response
	 * @param cookieName
	 * @param value
	 * @param cookieAge 以秒为单位
	 */
	public static void setCookieValue(HttpServletResponse response,
			String cookieName, String value, int cookieAge) {

		setCookieValue(response, cookieName, value, cookieAge, null, false);
	}
	
	/**
	 * 
	 * @param response
	 * @param cookieName
	 * @param value
	 * @param cookieAge 以秒为单位
	 */
	public static void setHttpOnlyCookie(HttpServletResponse response,
			String cookieName, String value, int cookieAge){
		setCookieValue(response, cookieName, value, cookieAge, null, true);
	}

	// public static void setCookieValue(HttpServletResponse response,
	// String cookieName, String value, int cookieAge, String cookiePath) {
	// Assert.notNull(response);
	// Assert.hasText(cookieName);
	// Assert.hasText(value);
	// // 特殊字符串处理
	// value = encode(value);
	// Cookie cookie = new Cookie(cookieName, value);
	// if (StringUtils.isBlank(cookiePath)) {
	// cookie.setPath("/");
	// } else {
	// cookie.setPath(cookiePath);
	// }
	// cookie.setMaxAge(cookieAge);
	// LOG.debug("2 setCookieValue:  " + value);
	// response.addCookie(cookie);
	// }

	/**
	 * 
	 * @param response
	 * @param cookieName
	 * @param value
	 * @param cookieAge 以秒为单位
	 * @param cookiePath
	 * @param httpOnly
	 */
	public static void setCookieValue(HttpServletResponse response,
			String cookieName, String value, int cookieAge,
			String cookiePath, boolean httpOnly) {

		Assert.notNull(response);
		Assert.hasText(cookieName);
		Assert.hasText(value);
		// 特殊字符串处理
		value = encode(value);
//		value = urlEncode(value);
//		response.setCharacterEncoding("UTF-8");
		Cookie cookie = new Cookie(cookieName, value);
		if (StringUtils.isBlank(cookiePath)) {
			cookie.setPath("/");
		} else {
			cookie.setPath(cookiePath);
		}
		
		String domain = getCookieDomain();
		cookie.setDomain(domain);
		cookie.setMaxAge(cookieAge);
		//cookie.setHttpOnly(httpOnly);
		response.addCookie(cookie);
	}
	
	private static String getCookieDomain(){
		String domain = BaseConfig.getValue("cookie.root.domain", ".baoxianjie.net");
		if(!BaseConfig.getBool("cookie.domain.fixed")){
			String currDomain = ThreadUtil.getCurrDomain();
			if(StringUtils.isNotBlank(currDomain)){
				domain = currDomain;
			}
		}
		return domain;
	}

	/**
	 * 获取某个url的domain
	 *
	 * @param url
	 * @return
	 */
	public static String getHost(String url) {

		try {
			String s = url;
			int pos = s.indexOf("//");
			if (pos > -1)
				s = s.substring(pos + 2);
			pos = s.indexOf("/");
			if (pos > -1)
				s = s.substring(0, pos);
			if (s.trim().length() == 0)
				s = "";
			return s;
		} catch (Exception exp) {
			return "";
		}
	}

	public static void deleteCookie(HttpServletRequest request,
			HttpServletResponse response, String... cookieNames) {

		deleteCookie(null, request, response, cookieNames);
	}

	/**
	 * 删除Cookie，使cookie马上过期(.meiliwan.com域)
	 * 
	 * @param request
	 * @param response
	 * @param path
	 *            cookie存放的路径
	 * @param cookieNames
	 * @return
	 */
	// public static String deleteCookie(HttpServletRequest request,
	// HttpServletResponse response, String path, String domain, String...
	// cookieNames) {
	// Assert.notNull(request);
	//
	// path = (path == null || path.equals("")) ? "/" : path;
	//
	// return deleteCookieDomain(request, response, path, domain, cookieNames);
	// }

	/**
	 * 删除Cookie，使cookie马上过期
	 * 
	 * @param request
	 * @param response
	 * @param path
	 *            cookie存放的路径
	 * @param domain
	 *            cookie存放的域
	 * @param cookieNames
	 * @return
	 */
	public static void deleteCookie(String path,
			HttpServletRequest request, HttpServletResponse response,
			String... cookieNames) {

		Assert.notNull(request);

		path = (path == null || path.equals("")) ? "/" : path;

		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			String domain = getCookieDomain();
//			domain = StringUtils.isBlank(domain) ? BaseConfig.getValue("cookie.root.domain", ".baoxianjie.net") : domain;
			for (Cookie cookie : cookies) {
				for (String cookieName : cookieNames) {
					Assert.hasText(cookieName);
					if (cookie.getName().equals(cookieName)) {
						cookie.setPath(path);
						cookie.setMaxAge(0);
						cookie.setDomain(domain);
						response.addCookie(cookie);
						break;
					}
				}

			}
		}
	}

	public static void deleteAllCookies(HttpServletRequest request,
			HttpServletResponse response) {

		Assert.notNull(request);
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
//				if (cookie.getDomain() != null
//						&& cookie.getDomain().equals(domain)) {
					cookie.setPath(StringUtils.isBlank(cookie.getPath()) ? "/" : cookie.getPath());
					cookie.setMaxAge(0);
					
					String domain = getCookieDomain();
					cookie.setDomain(domain);
					response.addCookie(cookie);
//				}
			}
		}
	}

	/**
	 * 普通的采用Gson将对象序列化成json字符串的方法
	 * 
	 * @param obj
	 * @param request
	 * @param response
	 */
	public static void writeJson(Object obj, HttpServletRequest request,
			HttpServletResponse response) {

		Map<Class, JsonSerializer> serializerMap = new HashMap<>();
		serializerMap.put(ResultCodeIntf.class, new ResultCodeSerializer());
		
		writeJson(obj, request, response, serializerMap);

		// try {
		// String callBack = ServletRequestUtils.getStringParameter(request,
		// "callback");
		// response.setHeader("Content-Language", "zh-cn");
		// response.setHeader("Cache-Control", "no-cache");
		// response.setContentType("application/json; charset=UTF-8");
		// response.setCharacterEncoding("UTF-8");
		//
		// String result = null;
		// if (obj instanceof String) {
		// result = obj.toString();
		// } else {
		// result = new Gson().toJson(obj);
		// }
		//
		// if (StringUtils.isNotBlank(callBack)) {
		// result = callBack + "(" + result + ")";
		// }
		//
		// response.getWriter().write(result);
		// } catch (IOException e) {
		// LOG.error(e.getMessage(), e);
		// } catch (ServletRequestBindingException e) {
		// LOG.error(
		// "[写json时候异常]" + obj + ", ip:" + WebUtils.getIpAddr(request),
		// e);
		// }
	}

	/**
	 * 普通的采用Gson将对象序列化成json字符串的方法 屏蔽存在JsonIgnore注解的字段
	 * 
	 * @param obj
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("all")
	public static void writeJson4ResultModel(Object obj,
			HttpServletRequest request, HttpServletResponse response) {

		Map<Class, JsonSerializer> serializerMap = new HashMap<Class, JsonSerializer>();
		serializerMap.put(ResultCodeIntf.class, new ResultCodeSerializer());

		writeJson(obj, request, response, serializerMap);
	}

	@SuppressWarnings("all")
	public static void writeJson(Object obj, HttpServletRequest request, HttpServletResponse response,
			Map<Class, JsonSerializer> serializerMap) {

		try {
			String callBack = ServletRequestUtils.getStringParameter(request,
					"callback");
			response.setHeader("Content-Language", "zh-cn");
			response.setHeader("Cache-Control", "no-cache");
			response.setContentType("application/json; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");

			String result = null;
			if (obj instanceof String) {
				result = obj.toString();
			} else {
				/*GsonBuilder builder = new GsonBuilder().setExclusionStrategies(new FieldIgnoreGsonStrategy());
				if (serializerMap != null && !serializerMap.isEmpty()) {
					for (Entry<Class, JsonSerializer> entry : serializerMap.entrySet()) {
						builder.registerTypeHierarchyAdapter(entry.getKey(), entry.getValue());
					}
				}
				result = builder.create().toJson(obj);*/
				
				result = JsonUtil.create().toJson(obj);
			}

			if (StringUtils.isNotBlank(callBack)) {
				result = callBack + "(" + result + ")";
			}

			response.getWriter().write(result);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (ServletRequestBindingException e) {
			LOG.error(
					"[写json时候异常]" + obj + ", ip:" + WebUtils.getIpAddr(request),
					e);
		}
	}

	/**
	 * 使用JPA的程序请使用该方法往response写json数据
	 * 
	 * @param obj
	 * @param response
	 */
	public static void writeJsonForJPA(Object obj, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			ObjectMapper mapper = HibernateAwareObjectMapper.getInstance();
			response.setHeader("Content-Language", "zh-cn");
			response.setHeader("Cache-Control", "no-cache");
			response.setContentType("application/json; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");

			if (obj instanceof Throwable) {
				Throwable tr = (Throwable) obj;
				String msg = tr.getMessage();
				Map<String, String> errors = Maps.newHashMap();
				errors.put("type", "error");
				errors.put("message", msg);
				mapper.writeValue(response.getWriter(), errors);
			} else {
				SimpleBeanPropertyFilter simpleBeanPropertyFilter = simpleBeanPropertyFilterContainer
						.get();
				FilterProvider filters = null;
				if (simpleBeanPropertyFilter != null) {
					filters = new SimpleFilterProvider().addFilter(
							DEFAULT_JSON_FILTER_NAME, simpleBeanPropertyFilter);
				} else {
					filters = serializeAllFilterProvider;
				}
				mapper.writer(filters).writeValue(response.getWriter(), obj);
			}

		} catch (IOException e) {
			LOG.error("[写json时候IO异常] " + obj, e);
		}
	}

	/**
	 * 这个方法获取到的浏览器IP，有可能是好几个代理ip的组合，建议仅在打印日志的时候使用
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {

		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Cdn-Src-Ip");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 如果有业务需要将ip地址存入数据库的话，建议使用该方法获取ip地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientIp(HttpServletRequest request) {

		String clientIp = getIpAddr(request);

		return StringUtils.isNotBlank(clientIp) ? clientIp.split(",")[0] : null;
	}

	

	public static String getEquipType(HttpServletRequest request) {

		String userAgent = request.getHeader("User-Agent");
		if (StringUtils.isNotBlank(userAgent)) {
			userAgent = userAgent.toUpperCase();
			if (userAgent.contains(ANDROID)) {
				return ANDROID;
			} else if (userAgent.contains(IPHONE)) {
				return IPHONE;
			} else if (userAgent.contains(IPAD)) {
				return IPAD;
			} else if (userAgent.contains(WINDOWS_PHONE)) {
				return WINDOWS_PHONE;
			} else if (userAgent.contains(IPOD)) {
				return IPOD;
			}
		}

		return PC;
	}

	/**
	 * 
	 * @param request
	 * @return 如果当前请求为ajax请求，返回true；否则返回false
	 */
	public static boolean isAjax(HttpServletRequest request) {
		String flag = request.getHeader("X-Requested-With");
		if(StringUtils.isBlank(flag)){
			flag = request.getParameter("X-Requested-With");
		}

		return ("XMLHttpRequest"
				.equals(flag));
	}
	
	/**
	 * 使用@ControllerAdvice的拦截器可以用到
	 * @param request
	 * @return
	 */
	public static boolean isAjax(WebRequest request){
		String flag = request.getHeader("X-Requested-With");
		if(StringUtils.isBlank(flag)){
			flag = request.getParameter("X-Requested-With");
		}

		return ("XMLHttpRequest"
				.equals(flag));
	}

	public static boolean isMobile(HttpServletRequest request) {

		String ua = request.getHeader("user-agent");
		// return true;
//		return StringUtils.isNotBlank(ua)
//				&& (ua.contains("Android") || ua.contains("iPad") || ua
//						.contains("Linux"));
		return RegexUtil.isMobileClient(ua);
	}
	
	/**
	 * 判断是否为微信客户端
	 * @param request
	 * @return
	 */
	public static boolean isWxClient(HttpServletRequest request){
		String ua = request.getHeader("user-agent");
		if(StringUtils.isBlank(ua)){
			return false;
		}
		
		return ua.toLowerCase().indexOf("micromessenger") >= 0;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param loginUrl
	 * @param refererKeyName
	 * @param needEncode 是否需要对当前url进行encode之后再追加到 loginUrl 后边
	 * @return
	 * @throws IOException
	 */
	public static String buildRedirectUrl(HttpServletRequest request, HttpServletResponse response, String loginUrl, String refererKeyName, boolean needEncode) throws IOException {
		String requestUrl = getCurrParamUrl(request);
		requestUrl = response.encodeURL(requestUrl);
		loginUrl = response.encodeURL(loginUrl);
		
		String encodeUrl = requestUrl;
		if(needEncode){
			encodeUrl = urlEncode(requestUrl);
			encodeUrl = urlEncode(encodeUrl);
		}

		return addParam(loginUrl, StringUtils.isBlank(refererKeyName) ? "referer" : refererKeyName, encodeUrl);
	}
	
	public static String buildRedirectUrl(HttpServletRequest request, HttpServletResponse response, String loginUrl) throws IOException {

//		String queryStr = StringUtils.isNotBlank(request.getQueryString()) ? "?"
//				+ request.getQueryString()
//				: "";
		return buildRedirectUrl(request, response, loginUrl, "referer", true);
	}
	
	/**
	 * 获取当前request完整URI，带参数
	 * @param request
	 * @return
	 */
	public static String getCurrParamUri(HttpServletRequest request) {
        String queryStr = StringUtils.isNotBlank(request.getQueryString()) ? "?"
				+ request.getQueryString()
				: "";
				
		return request.getServletPath() + queryStr;
	}
	
	/**
	 * 获取当前request完整URL，带参数
	 * 
	 * @param request
	 * @return
	 */
	public static String getCurrParamUrl(HttpServletRequest request){
		String queryStr = StringUtils.isNotBlank(request.getQueryString()) ? "?"
				+ request.getQueryString()
				: "";
		return request.getRequestURL().toString() + queryStr;
	}
	
	public static String parseRedirectUrl(HttpServletRequest request, boolean needDecode, String... excludeUriFlags){
		String referer = request.getParameter("referer");
		if(StringUtils.isBlank(referer)){
			referer = request.getHeader("referer");
			if(StringUtils.isNotBlank(referer)){
				referer = urlEncode(referer);
			}
		}
		if(StringUtils.isNotBlank(referer) && excludeUriFlags != null && excludeUriFlags.length > 0){
			for(String exludeUriFlag : excludeUriFlags){
				if(referer.contains(exludeUriFlag)){
					referer = "/";
					break;
				}
			}
		}
		
		return StringUtils.isBlank(referer) || "/".equals(referer) ? "/" : (needDecode ? urlDecode(referer) : referer);
	}
	
	public static boolean isStaticRes(String uri, Set<String> extendNames) {
		if(uri.startsWith("/static")){
			return true;
		}

		if(!CollectionUtils.isEmpty(extendNames)){
			for (String suffix : extendNames) {
				if (uri.endsWith(suffix)) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * 将参数name=value增加到url后边
	 * @param url
	 * @param name
	 * @param value
	 * @return
	 */
	public static String addParam(String url, String name, String value){
		if(StringUtils.isNotBlank(url) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)){
			url = url + (url.contains("?") ? "&" : "?") + name + "=" + value;
		}
		
		return url;
	}
	
	public static String urlDecode(String url){
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("url:" + url + ",errorMsg:" +e.getMessage(), e);
		}
		
		return url;
	}
	
	public static String urlEncode(String url){
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("url:" + url + ",errorMsg:" +e.getMessage(), e);
		}
		
		return url;
	}

	private static String encode(String json) {

		return json.replaceAll("\"", "\\&").replaceAll(",", "\\$");
	}

	private static String decode(String json) {

		return json.replaceAll("\\&", "\"").replaceAll("\\$", ",");
	}

	public static String getRequestJsonString(HttpServletRequest request)
	{

		StringBuilder sb = new StringBuilder();
		try
		{
			BufferedReader reader = request.getReader();
			char[] buff = new char[1024];
			int len;
			while ((len = reader.read(buff)) != -1)
			{
				sb.append(buff, 0, len);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
}
