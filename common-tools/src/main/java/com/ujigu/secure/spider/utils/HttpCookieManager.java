/**   
* @Title: HttpCookieManager.java 
* @Package com.ujigu.secure.spider.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年8月18日 上午10:37:43 
* @version V1.0   
*/
package com.ujigu.secure.spider.utils;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.CookieStore;

/**
 * @author user
 *
 */
public class HttpCookieManager {
	
	private static ConcurrentHashMap<String, CookieStore> cookieMaps = new ConcurrentHashMap<String, CookieStore>();
	
	
	/*
	 * 获得cookie
	 */
	public static CookieStore getCookies(String key){
		CookieStore cookieStore = cookieMaps.get(key);
		if(cookieStore != null){
			return cookieStore;
		}else{
			return null;
		}
	}
	
	/*
	 * set
	 */
	public static void setCookies(String key, CookieStore cookieStore){
		cookieMaps.put(key, cookieStore);
	}
	
	
	public static Date getNextYear(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1);
		return calendar.getTime();
	}
	
	
    /*
     * 解析host
     */
    public static String parseDomain(String urlAddress){  
    	try { 
	        URL url = new URL(urlAddress);  
	        return url.getHost();
		} catch (Exception e) {
			return null;
		}
    }  
	
}
