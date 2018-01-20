package com.ujigu.secure.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;


public class HttpClientUtils {
	private static PoolingHttpClientConnectionManager cm;
	private static String EMPTY_STR = "";
	private static String UTF_8 = "UTF-8";
	

	private static void init() {
		if (cm == null) {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(50);// 整个连接池最大连接数
			cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
		}
	}

	/**
	 * 通过连接池获取HttpClient
	 * 
	 * @return
	 */
	private static CloseableHttpClient getHttpClient() {
		init();
		return HttpClients.custom().setConnectionManager(cm).build();
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String httpGetRequest(String url) {
		HttpGet httpGet = new HttpGet(url);
		return getResult(httpGet);
	}

	public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException {
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);

		ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
		ub.setParameters(pairs);

		HttpGet httpGet = new HttpGet(ub.build());
		return getResult(httpGet);
	}

	public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params)
			throws URISyntaxException {
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);

		ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
		ub.setParameters(pairs);

		HttpGet httpGet = new HttpGet(ub.build());
		for (Map.Entry<String, Object> param : headers.entrySet()) {
			httpGet.addHeader(param.getKey(), toVal(param.getValue()));
		}
		return getResult(httpGet);
	}

	public static String httpPostRequest(String url) {
		HttpPost httpPost = new HttpPost(url);
		return getResult(httpPost);
	}

	public static String httpPostRequest(String url, Map<String, Object> params) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);
		ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
		return getResult(httpPost);
	}

	public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params)
			throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);

		for (Map.Entry<String, Object> param : headers.entrySet()) {
			httpPost.addHeader(param.getKey(), toVal(param.getValue()));
		}

		ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

		return getResult(httpPost);
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
	public static String httpPostRequestJson(String url,Map<String, Object> headers, String json) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);
		
		for (Map.Entry<String, Object> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), toVal(header.getValue()));
		}

		StringEntity entity = new StringEntity(json,"UTF-8");//解决中文乱码问题       
		entity.setContentType("application/json");

		httpPost.setEntity(entity);
		return getResult(httpPost);
	}
	
	/**
	 * 
	* @Title: httpPostRequestJson 
	* @Description: post调用xml参数
	* @param @param url
	* @param @param headers
	* @param @param xml
	* @param @return
	* @param @throws UnsupportedEncodingException
	* @return String 返回类型
	* @throws
	 */
	public static String httpPostRequestXml(String url,Map<String, Object> headers, String xml) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(url);
		
		for (Map.Entry<String, Object> header : headers.entrySet()) {
			httpPost.addHeader(header.getKey(), toVal(header.getValue()));
		}

		StringEntity entity = new StringEntity(xml,"utf-8");//解决中文乱码问题       
		entity.setContentType("text/xml");

		httpPost.setEntity(entity);
		return getResult(httpPost);
	}
	
	/**
	 * @param url
	 * @param headers
	 * @param param
	 * @param user
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException
	 * @Decription 需要BASIC认证的调用
	 * @Author Administrator
	 * @Time 2017年5月25日下午12:05:39
	 * @Exception
	 */
	public static String httpPostRequestAuth(String url,String param,String user,String password) throws UnsupportedEncodingException{
		
		HttpPost httpPost = new HttpPost(url);
		String authinfo = user + ":" + password;
		authinfo = new String(org.apache.commons.codec.binary.Base64.encodeBase64(authinfo.getBytes()));
		httpPost.addHeader("Authorization", "Basic " + authinfo);
		StringEntity entity = new StringEntity(param,"utf-8");
		entity.setContentType("application/json");

		httpPost.setEntity(entity);
		return getResult(httpPost);
	}
	
	/**
	 * @param url
	 * @param param
	 * @param store
	 * @param storePass
	 * @param storeType
	 * @param trustPass
	 * @param trustStore
	 * @return
	 * @throws IOException
	 * @Decription 证书认证SSL连接调用
	 * @Author Administrator
	 * @Time 2017年7月20日下午3:56:04
	 * @Exception
	 */
	public static String httpsPostXml(String url,String param,String store,String storePass,String storeType,String trustPass,String trustStore) throws IOException{
		URL conurl = new URL(url);
		System.setProperty("javax.net.ssl.keyStore", store);
        System.setProperty("javax.net.ssl.keyStorePassword", storePass);
        System.setProperty("javax.net.ssl.keyStoreType", storeType);
        System.setProperty("javax.net.ssl.trustStorePassword",trustPass);
        System.setProperty("javax.net.ssl.trustStore",trustStore);
        HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				LogUtils.info("Warning: URL Host: %s , vs. %s", hostname,session.getPeerHost());
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
        HttpsURLConnection connection = (HttpsURLConnection) conurl.openConnection();
        connection.setRequestProperty("Content-Type", "text/xml");
		connection.setDoOutput(true); 
		connection.setDoInput(true);
		connection.setConnectTimeout(100000);
		connection.setReadTimeout(100000);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
		out.write(param);
		out.flush();
		out.close();
		InputStream is = connection.getInputStream();
		InputStreamReader inReader = new InputStreamReader(is, "utf-8");
		BufferedReader aReader = new BufferedReader(inReader);
		String result = "";
		String inputLine;
		while ((inputLine = aReader.readLine()) != null) {
			result += inputLine;
		}
		inReader.close();
		aReader.close();
		connection.disconnect();
		return result;
	}
	
	private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Object> param : params.entrySet()) {
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
	 * 处理Http请求
	 * @param request
	 * @return
	 */
	private static String getResult(HttpRequestBase request) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			CloseableHttpResponse response = httpClient.execute(request);
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
	

}