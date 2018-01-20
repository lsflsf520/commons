/**   
* @Title: HtmlUnitUtils.java 
* @Package com.ujigu.secure.spider.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年8月17日 下午12:06:13 
* @version V1.0   
*/
package com.ujigu.secure.spider.utils;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.io.ByteStreams;
import com.ujigu.secure.common.utils.LogUtils;

/**
 * @author user
 *
 */
public class HtmlUnitUtils {
	
	//private static ConcurrentHashMap<String, Map<String, String>> cookieMaps = new ConcurrentHashMap<String, Map<String, String>>();
	
	
//	public static void main(String[] args) throws Exception {
//		//做的第一件事，去拿到这个网页，只需要调用getPage这个方法即可
//		//String url = "http://baoxian.csaimall.com/";
//		//HtmlPage htmlpage = webClient.getPage(url);
//		//System.out.println(htmlpage.asXml());
//		String url = "https://icore-pts.pingan.com.cn/ebusiness/login.jsp";
//		System.out.println("parseDomain:"+parseDomain(url));
//		System.out.println(HtmlUnitUtils.sendGetRequest("1",url));
//	}
	
	/*
	 * 创建客户端
	 */
	public static WebClient createWebClient(String upCompany) {
		WebClient webClient = createWebClient();
		if(upCompany != null){
			CookieStore cookieStore = HttpCookieManager.getCookies(upCompany);
			setCookies(webClient, cookieStore);
		}
		//返回对象
		return webClient;
	}

	
	/*
	 * 创建客户端
	 */
	public static WebClient createWebClient() {
		//WebClient webClient = new WebClient();
		WebClient  webClient=new WebClient(BrowserVersion.CHROME);
		// 禁用css
		webClient.getOptions().setCssEnabled(false);
		// 启用js
		webClient.getOptions().setJavaScriptEnabled(true);
		//启用cookies
		webClient.getCookieManager().setCookiesEnabled(true);
		// 启动客户端重定向
		webClient.getOptions().setRedirectEnabled(true);
		// js运行错误时，是否抛出异常
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		// 设置超时 30s
		webClient.getOptions().setTimeout(3000);
		//设置ajax刷新
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		//返回对象
		return webClient;
	}
	
	/**
	 * Get请求
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String sendGetRequest(String upCompany, String url){
		try {
			WebClient webClient = HtmlUnitUtils.createWebClient(upCompany);
			WebRequest webRequest = new WebRequest(new URL(url));
			webRequest.setHttpMethod(HttpMethod.GET);
			return sendRequest(upCompany, webClient, webRequest);
		} catch (Exception e) {
			LogUtils.error("HtmlUnit sendGetRequest Error! upCompany:%s ,url:%s", e, upCompany ,url);
			return "error";
		}

	}

	/**
	 * Post 请求
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String sendPostRequest(String upCompany, String url, Map<String, String> params, Map<String, String> headers){
		try {
			WebClient webClient = HtmlUnitUtils.createWebClient(upCompany);
			WebRequest webRequest = new WebRequest(new URL(url));
			webRequest.setHttpMethod(HttpMethod.POST);
			for (String key:headers.keySet()) {
				webRequest.setAdditionalHeader(key, headers.get(key));
			}
			webRequest.setRequestParameters(new ArrayList<NameValuePair>());
			if (params != null && params.size() > 0) {
				for (Entry<String, String> param : params.entrySet()) {
					webRequest.getRequestParameters().add(new NameValuePair(param.getKey(), param.getValue()));
				}
			}
			return sendRequest(upCompany, webClient, webRequest);
		} catch (Exception e) {
			LogUtils.error("HtmlUnit sendPostRequest Error! upCompany:%s ,url:%s", e, upCompany ,url);
			return "error";
		}
		
	}

	
	
	//底层请求
	private static String sendRequest(String upCompany, WebClient webClient, WebRequest webRequest) throws Exception {
		byte[] responseContent = null;
		Page page = webClient.getPage(webRequest);
		WebResponse webResponse = page.getWebResponse();
		int status = webResponse.getStatusCode();
		//读取数据内容
		if (status == 200) {
			if (page.isHtmlPage()) {
				//等待JS执行完成，包括远程JS文件请求，Dom处理
				webClient.waitForBackgroundJavaScript(3*1000);
				//使用JS还原网页
				responseContent = ((HtmlPage)page).asXml().getBytes();
			} else {
				InputStream bodyStream = webResponse.getContentAsStream();
				responseContent = ByteStreams.toByteArray(bodyStream);
				bodyStream.close();
			}
		}
		
		//cookies保存
		CookieStore cookieStore = HttpCookieManager.getCookies(upCompany);
		cookieStore = getResponseCookies(cookieStore, webClient, webRequest);
		HttpCookieManager.setCookies(upCompany, cookieStore);
		
		//关闭响应流
		webResponse.cleanUp();
		webClient.close();
		return new String(responseContent);
	}
	
	/**
	 * 
	* @Title: getResponseCookies 
	* @Description: 获得cookies
	* @param @param webClient
	* @param @return
	* @return Map<String,String> 返回类型
	* @throws
	 */
    private static CookieStore getResponseCookies(CookieStore cookieStore, WebClient webClient, WebRequest webRequest) {  
    	if(cookieStore == null){
    		cookieStore = new BasicCookieStore();
    	}
    	Set<Cookie> cookies = webClient.getCookieManager().getCookies();  
        String host = webRequest.getUrl().getHost();
        for (Cookie c : cookies) {  
        	BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
			cookie.setDomain(host);
			cookie.setPath("/");
			cookieStore.addCookie(cookie);
        } 
        return cookieStore;  
    }  
    
    /**
     * 
    * @Title: setCookies 
    * @Description: 设置cookies
    * @param @param webClient
    * @param @param domain
    * @param @param cookies
    * @return void 返回类型
    * @throws
     */
    public static void setCookies(WebClient webClient,CookieStore cookieStore) {  
    	if(cookieStore == null){
    		cookieStore = new BasicCookieStore();
    	}
    	List<org.apache.http.cookie.Cookie> cookiesList = cookieStore.getCookies();
    	for (org.apache.http.cookie.Cookie httpCookie : cookiesList) {
    		Cookie cookie = new Cookie(httpCookie.getDomain(), httpCookie.getName(), httpCookie.getValue());  
    		webClient.getCookieManager().addCookie(cookie);
		}
    }
    
}
