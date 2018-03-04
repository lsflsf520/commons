package com.xyz.tools.web.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.reflect.TypeToken;
import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.constant.GlobalConstant;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.HttpClientUtils;
import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.common.utils.JsonUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.ThreadUtil;
import com.xyz.tools.common.utils.LogUtils.IntfType;

/**
 * 
 * @author lsf
 *
 */
public class RestClientUtil {
	
	private final static Map<String, RestClient> RC_MAP = new HashMap<>();
	
	private final static int DEFAULT_READ_TIMEOUT = 30000;
	
	/**
	 * 
	 * @param url 网络地址
	 * @param dirPath
	 * @param fileName
	 * @return 返回下载文件流后的磁盘绝对路径
	 */
	public static String downloadFile(String src, String dirPath, String fileName){
		String filepath = dirPath+File.separator+fileName;
		
		InputStream is = null;
		OutputStream os = null;
		try {
			URL url = new URL(src);
			URLConnection conn = url.openConnection();
	        //获取数据流
	        is = conn.getInputStream();
	        //写入数据流
	        File dir = new File(dirPath);
	        //判断文件目录是否存在
	        if(!dir.exists()){
	            dir.mkdirs();
	        }
	        os = new FileOutputStream(new File(filepath));
	        byte[] buf = new byte[1024];
	        int l=0;
	        while((l=is.read(buf))!=-1){
	            os.write(buf, 0, l);
	        }
	        
	        return filepath;
		} catch (MalformedURLException e) {
			LogUtils.warn("write file(%s) error, errorMsg:" + e.getMessage(), filepath);
		} catch (IOException e) {
			LogUtils.warn("write file(%s) error, errorMsg:" + e.getMessage(), filepath);
		} finally {
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
					LogUtils.warn("close os stream error after write file(%s), errorMsg:" + e.getMessage(), filepath);
				}
			}
			
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					LogUtils.warn("close is stream error after write file(%s), errorMsg:" + e.getMessage(), filepath);
				}
			}
		}
        
		return null;
	}
	
	/**
	 * 
	 * @param fileBytes 文件流字节数组
	 * @param dirPath 需要下载到的目录
	 * @param fileName 下载后的文件名
	 * @return 返回下载文件流后的磁盘绝对路径
	 */
	public static String saveFile(byte[] fileBytes,String dirPath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        File dir = new File(dirPath);
        String filepath = dirPath+File.separator+fileName;
        try {
            //判断文件目录是否存在
            if(!dir.exists()){
                dir.mkdirs();
            }
            file = new File(filepath);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(fileBytes);
            
            return filepath;
        } catch (FileNotFoundException e) {
            LogUtils.warn("write file(%s) error, errorMsg:" + e.getMessage(), filepath);
        } catch (IOException e) {
        	LogUtils.warn("write file(%s) error, errorMsg:" + e.getMessage(), filepath);
        }finally{
            if(bos!=null){
                try {
                    bos.close();
                } catch (IOException e) {
                	LogUtils.warn("close bos stream error after write file(%s), errorMsg:" + e.getMessage(), filepath);
                }
            }
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                	LogUtils.warn("close fos stream error after write file(%s), errorMsg:" + e.getMessage(), filepath);
                }
            }
        }
        
        return null;
    }
	
	/**
	 * @TODO 未做测试
	 * @param url
	 * @param formParams
	 * @param headerParams
	 * @param readTimeout
	 * @param responseType
	 * @return
	 */
	public static <T> T doPost(String url, Map<String, ?> formParams, Map<String, String> headerParams, int readTimeout, Class<T> responseType){
		if(MapUtils.isEmpty(headerParams)){
			return doPost(url, formParams, readTimeout, responseType);
		}
		
		HttpHeaders headers = new HttpHeaders();
		for(String hkey : headerParams.keySet()){
			headers.add(hkey, headerParams.get(hkey));
		}

        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpEntity<T> response = getRestClient(readTimeout).exchange(url, HttpMethod.POST, entity, responseType, formParams);
        
        return response.getBody();
	}
	
	/**
	 * 
	 * @param url
	 * @param formParams
	 * @param responseType
	 * @return
	 */
    public static <T> T doPost(String url, Map<String, ?> formParams, Class<T> responseType) {
    	return doPost(url, formParams, DEFAULT_READ_TIMEOUT, responseType);
    }
	
	/**
	 * 
	 * @param url
	 * @param formParams
	 * @param readTimeout
	 * @param responseType
	 * @return
	 */
    public static <T> T doPost(String url, Map<String, ?> formParams, int readTimeout, Class<T> responseType) {
        if (MapUtils.isEmpty(formParams)) {
            return doPost(url, readTimeout, responseType);
        }

        try {
            MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
            for(String key : formParams.keySet()){
            	String val = toVal(formParams.get(key));
            	requestEntity.add(key, val);
            }
            return getRestClient(readTimeout).postForObject(url, requestEntity, responseType);
        } catch (Exception e) {
            throw new BaseRuntimeException("REQ_URL_ERROR", "网络请求错误", "url:" + url, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T doJsonPost(String url, Map<String, ?> jsonParams, Class<T> responseType){
    	String str = null;
    	try {
			str = HttpClientUtils.httpPostRequestJson(url, new HashMap<String,Object>(), toVal(jsonParams));
			if(StringUtils.isBlank(str) 
					|| "java.lang.String".equals(responseType.getName())){
				return (T)str;
			}
			
			return JsonUtil.create().fromJson(str, responseType);
		} catch (Exception e) {
			 throw new BaseRuntimeException("REQ_URL_ERROR", "网络请求错误", "url:" + url + ",jsonParams:" + jsonParams + ",str:" +str, e);
		}
//    	return doJsonPost(url, jsonParams, DEFAULT_READ_TIMEOUT, responseType);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T doJsonPost(String url, List<?> list, Class<T> responseType){
    	String str = null;
    	try {
			str = HttpClientUtils.httpPostRequestJson(url, new HashMap<String,Object>(), toVal(list));
			if(StringUtils.isBlank(str) 
					|| "java.lang.String".equals(responseType.getName())){
				return (T)str;
			}
			
			return JsonUtil.create().fromJson(str, responseType);
		} catch (Exception e) {
			 throw new BaseRuntimeException("REQ_URL_ERROR", "网络请求错误", "url:" + url + ",jsonParams:" + list + ",str:" +str, e);
		}
    }
    
    /**
     * 
     * @param url
     * @param jsonParams
     * @param readTimeout
     * @param responseType
     * @return
     */
    /*public static <T> T doJsonPost(String url, Map<String, ?> jsonParams, int readTimeout, Class<T> responseType){
    	if (MapUtils.isEmpty(jsonParams)) {
            return doPost(url, readTimeout, responseType);
        }

        try {
            String jsonbody = toVal(jsonParams);
            StringEntity se = new StringEntity(jsonbody, "utf-8");
            return getRestClient(readTimeout).postForObject(url, se, responseType);
        } catch (Exception e) {
            throw new BaseRuntimeException("REQ_URL_ERROR", "网络请求错误", "url:" + url, e);
        }
    }*/
    
    /**
     * 
     * @param url 
     * @param responseType 
     * @return
     */
    public static <T> T doPost(String url, Class<T> responseType) {
    	return doPost(url, DEFAULT_READ_TIMEOUT, responseType);
    }

    /**
     * 
     * @param url
     * @param readTimeout
     * @param responseType
     * @return
     */
    public static <T> T doPost(String url, int readTimeout, Class<T> responseType) {
        try {
            return getRestClient(readTimeout).postForObject(url, HttpEntity.EMPTY, responseType);
        } catch (Exception e) {
        	throw new BaseRuntimeException("REQ_URL_ERROR", "网络请求错误", "url:" + url, e);
        }

    }
    
    /**
     * Given an array of variables, expand this template into a full URI. The array represent variable values.
     * The order of variables is significant.
     * <p>Example:
     * <pre class="code">
     * url = "http://example.com/hotels/{hotel}/bookings/{booking}";
     * doGet(url, 5000,  String.class, "Rest & Relax", "42);
     * </pre>
     * equals: <blockquote>{@code doGet("http://example.com/hotels/Rest%20%26%20Relax/bookings/42", 5000,  String.class)}</blockquote>
     * @param url 
     * @param uriVarVals the array of URI variables
     * @return the expanded URI
     * @throws IllegalArgumentException if {@code uriVariables} is {@code null}
     * or if it does not contain sufficient variables
     */
    public static <T> T doGet(String url, Class<T> responseType, Object... uriVarVals){
    	return doGet(url, DEFAULT_READ_TIMEOUT, responseType, uriVarVals);
    }

    /**
     * Given an array of variables, expand this template into a full URI. The array represent variable values.
     * The order of variables is significant.
     * <p>Example:
     * <pre class="code">
     * url = "http://example.com/hotels/{hotel}/bookings/{booking}";
     * doGet(url, 5000,  String.class, "Rest & Relax", "42);
     * </pre>
     * equals: <blockquote>{@code doGet("http://example.com/hotels/Rest%20%26%20Relax/bookings/42", 5000,  String.class)}</blockquote>
     * @param url 
     * @param readTimeout 
     * @param uriVarVals the array of URI variables
     * @return the expanded URI
     * @throws IllegalArgumentException if {@code uriVariables} is {@code null}
     * or if it does not contain sufficient variables
     */
    public static <T> T doGet(String url, int readTimeout, Class<T> responseType, Object... uriVarVals) {
        try {
        	List<Object> vals = new ArrayList<>();
        	if(uriVarVals != null){
        		for(Object val : uriVarVals){
        			vals.add(toVal(val));
        		}
        	}
            return getRestClient(readTimeout).getForObject(url, responseType, vals.toArray(new Object[0]));
        } catch (Exception e) {
        	throw new BaseRuntimeException("REQ_URL_ERROR", "网络请求错误", "url:" + url, e);
        }

    }
    
    private static String toVal(Object valObj){
		String val = null;
		if(valObj != null){
			if(valObj instanceof String){
				val = String.valueOf(valObj);
			}else{
				val = JsonUtil.create().toJson(valObj);
			}
		}
		return val;
	}
    
    private static RestClient getRestClient(int readTimeout){
    	RestClient client = RC_MAP.get("" + readTimeout);
    	if(client == null){
    		client = new RestClient(readTimeout);
    		RC_MAP.put("" + readTimeout, client);
    	}
    	
    	return client;
    }
    
    private static class ResultModelMapper extends ObjectMapper{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ResultModelMapper() {
    		SimpleModule module = new SimpleModule("RestCustomJSONModule", new Version(1, 0, 0, null, null, null));
//       	    module.addSerializer(PageList.class, new PageListJsonSerializer(this));
//       	    module.addDeserializer(type, deser);
    		module.addDeserializer(ResultModel.class, new ResultModelDeserializer());
            registerModule(module);
		}
    	
    }
    
    private static class ResultModelDeserializer extends JsonDeserializer<ResultModel>{

		@Override
		public ResultModel deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			JsonNode node = jp.getCodec().readTree(jp);  
	        String resultCode = node.get("resultCode").asText(); 
	        String resultMsg = node.get("resultMsg").asText();  
	        String model = null;
	        if(node.get("model") != null){
	        	model = node.get("model").asText();  
	        }
	        if(model != null && !"null".equalsIgnoreCase(model)){
	        	if(model.startsWith("{")){
	        		try{
	        			Map<String, Object> dataModel = JsonUtil.create().fromJson(model, new TypeToken<TreeMap<String, Object>>() {
	        			}.getType());
	        			
	        			return new ResultModel(dataModel);
	        		} catch(Exception e){
	        			LogUtils.warn("error parse model(%s)", model);
	        		}
	        	} 

	        	return new ResultModel(model);
	        }
//	        int userId = (Integer) ((IntNode) node.get("id")).numberValue();
			
			return new ResultModel(resultCode, resultMsg);
		}
    	
    }
    
    private static class RestClient extends RestTemplate{
    	
    	private static List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    	static{
    		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
            messageConverters.add(new FormHttpMessageConverter());
//            messageConverters.add(new MappingJackson2HttpMessageConverter());
            
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            converter.setObjectMapper(new ResultModelMapper());
            messageConverters.add(converter);
            
            
    	}
    	
    	/**
    	 * 
    	 * @param readTimeout 数据读取超时时间，即SocketTimeout，以毫秒为单位
    	 */
    	public RestClient(int readTimeout){
    		// 添加内容转换器
            super(messageConverters);
    		 // 长连接保持30秒
            PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
            // 总连接数
            pollingConnectionManager.setMaxTotal(200);
            // 到同一目标主机的最大并发连接数
            pollingConnectionManager.setDefaultMaxPerRoute(40);

            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            httpClientBuilder.setConnectionManager(pollingConnectionManager);
            // 重试次数，默认是3次，没有开启
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            // 保持长连接配置，需要在头添加Keep-Alive
            httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
            
            /*此处解释下MaxtTotal和DefaultMaxPerRoute的区别：
            1、MaxtTotal是整个池子的大小；
            2、DefaultMaxPerRoute是根据连接到的主机对MaxTotal的一个细分；比如：
            MaxtTotal=400 DefaultMaxPerRoute=200
                                 而我只连接到http://sishuok.com时，到这个主机的并发最多只有200；而不是400；
                                 而我连接到http://sishuok.com 和 http://qq.com时，到每个主机的并发最多只有200；即加起来是400（但不能超过400）；所以起作用的设置是DefaultMaxPerRoute。
    */
//            RequestConfig.Builder builder = RequestConfig.custom();
//            builder.setConnectionRequestTimeout(200);
//            builder.setConnectTimeout(5000);
//            builder.setSocketTimeout(5000);
    //
//            RequestConfig requestConfig = builder.build();
//            httpClientBuilder.setDefaultRequestConfig(requestConfig);

            List<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"));
            headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
            headers.add(new BasicHeader("Accept-Language", "zh-CN"));
            headers.add(new BasicHeader("Connection", "Keep-Alive"));
            headers.add(new BasicHeader("X-Requested-With", "XMLHttpRequest"));
            headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8"));

            httpClientBuilder.setDefaultHeaders(headers);

            HttpClient httpClient = httpClientBuilder.build();

            // httpClient连接配置，底层是配置RequestConfig
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            // 连接超时
            clientHttpRequestFactory.setConnectTimeout(5000);
            // 数据读取超时时间，即SocketTimeout
            clientHttpRequestFactory.setReadTimeout(readTimeout);
            // 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
//            clientHttpRequestFactory.setConnectionRequestTimeout(200);
            // 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
//             clientHttpRequestFactory.setBufferRequestBody(false);

            setRequestFactory(clientHttpRequestFactory);
            setErrorHandler(new DefaultResponseErrorHandler());
            
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new CommonReqInterceptor());
            setInterceptors(interceptors); //设置请求拦截器
    	}
    	
    	static class CommonReqInterceptor implements ClientHttpRequestInterceptor{

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				String reqBody = new String(body,"UTF-8");
				LogUtils.logIntf(IntfType.OUT, request.getURI().toString(), reqBody);
				String msgId = ThreadUtil.getTraceMsgId();
				
				request.getHeaders().add(ThreadUtil.TRACE_MSG_ID, msgId);
				request.getHeaders().add(ThreadUtil.SRC_PROJECT_KEY, GlobalConstant.PROJECT_NAME);
				request.getHeaders().add("x-forwarded-for", IPUtil.getLocalIp());
				
				ClientHttpResponse response = execution.execute(request, body);
				
				LogUtils.logIntf(IntfType.IN, request.getURI().toString(), "code:%s, status:%s", response.getStatusCode(), response.getStatusText());
				 
//				new BufferingClientHttpResponseWrapper(response);

			    return response;
			}
    		
    	}

    }
}
