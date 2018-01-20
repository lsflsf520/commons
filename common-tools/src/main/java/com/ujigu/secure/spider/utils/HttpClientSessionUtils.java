package com.ujigu.secure.spider.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.ujigu.secure.common.utils.ImageUtils;
import com.ujigu.secure.common.utils.LogUtils;


public class HttpClientSessionUtils {
	private static PoolingHttpClientConnectionManager cm;
	private static String EMPTY_STR = "";
	private static String UTF_8 = "UTF-8";
	
	//private static Map<String, CookieStore> cookieMaps = new HashMap<String, CookieStore>();

	private static void init() {
		if (cm == null) {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(50);// 整个连接池最大连接数
			cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
		}
	}

	
	/**
	 * 通过连接池获取HttpClient
	 * @return
	 */
	private static CloseableHttpClient getHttpClient(String cookieKey) {
		init();
		CloseableHttpClient client = null;
		//组装cookies
		CookieStore	cookieStore = HttpCookieManager.getCookies(cookieKey);
		if(cookieStore != null){
			//使用cookie
			client = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
		}else{
			client = HttpClients.custom().setConnectionManager(cm).build();
		}
		return client;
	}

	
	/**
	* @Title: httpPostRequest 
	* @Description: 发送post请求
	* @param @param url
	* @param @param headers
	* @param @param params
	* @param @return
	* @param @throws UnsupportedEncodingException
	* @return String 返回类型
	* @throws
	 */
	public static String httpPostRequest(String url, Map<String, String> headers, Map<String, String> params, String key) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);

		for (Map.Entry<String, String> param : headers.entrySet()) {
			httpPost.addHeader(param.getKey(), toVal(param.getValue()));
		}
		
		ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
		
		return getResult(httpPost, headers, params, key);
	}
	
	
	/**
	* @Title: httpPostRequest 
	* @Description: 发送post请求
	* @param @param url
	* @param @param headers
	* @param @param params
	* @param @return
	* @param @throws UnsupportedEncodingException
	* @return String 返回类型
	* @throws
	 */
	public static String httpPostRequestNo302(String url, Map<String, String> headers, Map<String, String> params, String key) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);

		for (Map.Entry<String, String> param : headers.entrySet()) {
			httpPost.addHeader(param.getKey(), toVal(param.getValue()));
		}
		
		ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
		
		return getResultNo302(httpPost, headers, params, key);
	}

	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String httpGetRequest(String url, String key) {
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet,key);
	}

	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String httpPicRequest(String url, String key) {
		HttpGet httpGet = new HttpGet(url);
		return getResultInputStream(httpGet,key);
	}
	
	
	
	/**
	 * 
	* @Title: httpPostRequestJson 
	* @Description: post调用json参数
	* @param @param url
	* @param @param headers
	* @param @param json
	* @param @return
	* @param @throws UnsupportedEncodingException
	* @return String 返回类型
	* @throws
	 */
	public static String httpPostRequestJson(String url,Map<String, String> headers, String json, String key) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);
		
		for (Map.Entry<String, String> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), toVal(header.getValue()));
		}

		StringEntity entity = new StringEntity(json,"utf-8");//解决中文乱码问题       
		entity.setContentType("application/json");

		httpPost.setEntity(entity);
		return getResultJson(httpPost, key, url);
	}
	
	
	/**
	 * 处理Http请求
	 * @param request
	 * @return
	 */
	private static String getResultJson(HttpRequestBase request, String key, String url) {
		CloseableHttpClient httpClient = getHttpClient(key);
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			
			//保存当次cookie
			setCookies(request, key, response);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				response.close();
				return result;
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

		return EMPTY_STR;
	}
	
	
	/**
	 * 处理Http请求
	 * @param request
	 * @return
	 */
	private static String getResultInputStream(HttpRequestBase request, String key) {
		CloseableHttpClient httpClient = getHttpClient(key);
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			byte[] data = EntityUtils.toByteArray(entity);
			
			//保存当次cookie
			setCookies(request, key, response);
			
			return Base64.encodeBase64String(data);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
		return null;
	}
	
	
	/**
	 * 处理Http请求
	 * @param request
	 * @return
	 */
	private static String getResult(HttpRequestBase request, String key) {
		CloseableHttpClient httpClient = getHttpClient(key);
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			
			//保存当次cookie
			setCookies(request, key, response);
			
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				response.close();
				return result;
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

		return EMPTY_STR;
	}
	
	
	
	/**
	 * 处理Http请求
	 * @param request
	 * @return
	 */
	private static String getResultNo302(HttpRequestBase request, Map<String, String> headers, Map<String, String> params, String key) {
		CloseableHttpClient httpClient = getHttpClient(key);
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			
			//保存当次cookie
			setCookies(request, key, response);
			
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				response.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}

		return EMPTY_STR;
	}
	
	
	/**
	 * 处理Http请求
	 * @param request
	 * @return
	 */
	private static String getResult(HttpRequestBase request, Map<String, String> headers, Map<String, String> params, String key) {
		CloseableHttpClient httpClient = getHttpClient(key);
		try {
			CloseableHttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			
			//处理http返回码302的情况  
			if (response.getStatusLine().getStatusCode() == 302) {  
			    String locationUrl=response.getLastHeader("Location").getValue(); 
			    response.close();
			    LogUtils.info("302:%s", locationUrl);
			    return httpPostRequest(locationUrl, headers, params, key);
			    //get(locationUrl);//跳转到重定向的url  
			}
			
			//保存当次cookie
			setCookies(request, key, response);
			
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				response.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}

		return EMPTY_STR;
	}
	
	
	private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, String> params) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> param : params.entrySet()) {
			String val = toVal(param.getValue());
			pairs.add(new BasicNameValuePair(param.getKey(), val));
		}

		return pairs;
	}
	
	
	private static String toVal(Object valObj){
		String val = null;
		if(valObj != null){
			if(valObj instanceof String){
				val = String.valueOf(valObj);
			}else{
				val = new Gson().toJson(valObj);
			}
		}
		return val;
	}
	
	
	/**
	 * 
	* @Title: setCookies 
	* @Description: 设置cookies
	* @param @param key
	* @return void 返回类型
	* @throws
	 */
	private static void setCookies(HttpRequestBase request, String key, CloseableHttpResponse response){
		String host = request.getURI().getHost();
		CookieStore cookieStore = HttpCookieManager.getCookies(key);
		if(cookieStore == null){
			cookieStore = new BasicCookieStore();
		}
		try {
			Header headers = response.getFirstHeader("Set-Cookie");
			String cookieStr = "";
	        if (headers != null){  
	        	cookieStr = headers.getValue();
	        	
	        	System.out.println("cookiStr:"+cookieStr);
	        	System.out.println("host:"+host);
	        	String[] cookieArray = cookieStr.split(";");
				for (String singleCookie : cookieArray) {
					String[] singleCookieArray = singleCookie.split("=");
					if(singleCookieArray.length == 2){
						BasicClientCookie cookie = new BasicClientCookie(singleCookieArray[0].trim(), singleCookieArray[1].trim());
						cookie.setDomain(host);
						cookie.setPath("/");
						cookieStore.addCookie(cookie);
					}
				}
				HttpCookieManager.setCookies(key, cookieStore);
	        }else{
	        	System.out.println("there are no cookies");  
	        }	
		} catch (Exception e) {
			LogUtils.warn("cookie parse error",e);
		}
	}
	
	
//	public static void main(String[] args) {
//		String cookieStr = "MEDIA_SOURCE_NAME=ptsweb; WLS_HTTP_BRIDGE_ICOREPTS=gM1nZ6YLx8pm1nGnQGMj2dDvSxnCqmRflFRzT6YcKRSpBPjtSpRv!800451117; BIGipServericore-pts_http_ng_PrdPool=2356223148.62325.0000; _WL_AUTHCOOKIE_WLS_HTTP_BRIDGE_ICOREPTS=c3Iv2Xg8fopzyln-qqB6";
//		String[] cookieArray = cookieStr.split(";");
//		for (String singleCookie : cookieArray) {
//			String[] singleCookieArray = singleCookie.split("=");
//			System.out.println(singleCookieArray[0].trim()+":"+singleCookieArray[1].trim());
//		}
//	
//	}
}