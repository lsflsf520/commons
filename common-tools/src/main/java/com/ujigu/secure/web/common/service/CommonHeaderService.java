package com.ujigu.secure.web.common.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.ujigu.secure.cache.constant.DefaultJedisKeyNS;
import com.ujigu.secure.cache.redis.ShardJedisTool;
import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.web.common.service.CommonHeaderLoader.CommonHeader;
import com.ujigu.secure.web.common.service.headloader.ContextHeaderLoader;
import com.ujigu.secure.web.util.UserLoginUtil;

/**
 * 
 * @author lsf
 *
 */
public class CommonHeaderService implements InitializingBean{
	
	private final static Logger LOG = LoggerFactory.getLogger(CommonHeaderService.class);
	
	private List<CommonHeaderLoader> loaders;
	
	private final static Map<String/*acceptUri*/, CommonHeaderLoader> loaderMap = new HashMap<>();
	private final static Map<String/*acceptUri pattern*/, CommonHeaderLoader> patternLoaderMap = new HashMap<>();
	private final static Map<String/*acceptUri pattern*/, List<String>/*ordered path param names*/> patternUri2ParamNameMap = new HashMap<>();
	
//	private final static Set<String> cachedUris = new ConcurrentHashSet<>();
	
	private static AntPathMatcher urlMatcher = new AntPathMatcher();

	@Override
	public void afterPropertiesSet() throws Exception {
		if(loaders != null){
			for(CommonHeaderLoader loader : loaders){
				loadHeader(loader, true);
			}
		}
	}
	
	/**
	 * 直接删除完全匹配的uri缓存
	 * @param uri
	 */
	public static void delExactUri(String uri){
		ShardJedisTool.del(DefaultJedisKeyNS.common_header, uri);
	}
	
	/**
	 * 解析servletUri中匹配到的参数，并设置到request的Attribute中，便于后续调用
	 * @param request
	 * @param servletUri
	 * @param patternUri 能匹配到servletUri的 uri 模式，该模式从spring的配置属性中分析获得，参见 loadHeader(CommonHeaderLoader loader, boolean isInit) 方法
	 */
	private static void parsePathParams(HttpServletRequest request, String servletUri, String patternUri){
		List<String> pathParamNames = patternUri2ParamNameMap.get(patternUri);
		if(!CollectionUtils.isEmpty(pathParamNames)){
			String regex = patternUri.replace("*", "([^\\.]+)");
			List<String> vals = RegexUtil.extractGroups(regex, servletUri);
			if(!CollectionUtils.isEmpty(vals) && vals.size() == pathParamNames.size()){
				for(int index = 0; index < pathParamNames.size(); index++){
					request.setAttribute(pathParamNames.get(index), vals.get(index));
				}
			}
		}
	}
	
	private static CommonHeader getCache(String uri){
		String value = ShardJedisTool.get(DefaultJedisKeyNS.common_header, uri);
		if(StringUtils.isNotBlank(value) && !"{}".equals(value)){
			CommonHeader header = new Gson().fromJson(value, CommonHeader.class);
			if(header != null && !header.isEmpty()){
				return header;
			}
		}
		return null;
	}
	
	private static void setCache(String uri, CommonHeader commHeader){
		if(commHeader == null || commHeader.isEmpty()){
			return;
		}
		ShardJedisTool.set(DefaultJedisKeyNS.common_header, uri, new Gson().toJson(commHeader));
//		cachedUris.add(uri);
	}
	
	private static void delCache(String uri){
		if(StringUtils.isBlank(uri)){
			return;
		}
		
		ShardJedisTool.del(DefaultJedisKeyNS.common_header, uri);
		
		/*Set<String> needRemoveUris = new HashSet<>();
		for(String cachedUri : cachedUris){
			if(StringUtils.isBlank(cachedUri)){
				continue;
			}
			if(cachedUri.startsWith(uri)){
				needRemoveUris.add(cachedUri);
			}
		}
		
		if(!CollectionUtils.isEmpty(needRemoveUris)){
			for(String nruri : needRemoveUris){
				ShardJedisTool.del(DefaultJedisKeyNS.common_header, nruri);
				cachedUris.remove(uri);
			}
		}*/
	}
	
	public static CommonHeader getHeader(HttpServletRequest request, ModelAndView mav){
		String servletUri = request.getServletPath();
		String qstr = request.getQueryString();
		CommonHeaderLoader loader = loaderMap.get(servletUri);
		CommonHeader header = null;
		if(!(loader instanceof ContextHeaderLoader)){//ContextHeadLoader 加载的标题信息，无需缓存
			header = getCache(servletUri);
			if(header == null && StringUtils.isNotBlank(qstr)){
				header = getCache(servletUri + "?" + qstr);
			}
		}
		if(header == null && request != null){
			if(loader == null){//假如没有直接匹配到的uri，则从模式匹配的uri patternLoaderMap中查看是否有匹配到的uri
				for(String patternUri : patternLoaderMap.keySet()){
					if(urlMatcher.match(patternUri, servletUri)){
						loader = patternLoaderMap.get(patternUri);
						parsePathParams(request, servletUri, patternUri);
						break;
					}
				}
			}
			
			if(loader != null){
				List<String> paramNames = null;
				Map<String, Object> kvMap = new HashMap<>();
				String queryStr = null;
				if((paramNames = loader.getDynamicParamNames()) != null){
					StringBuilder builder = new StringBuilder();
					Collections.sort(paramNames);//这里排序是为了后边为对应的数据生成缓存的key
					for(String paramName : paramNames){
						String valstr = request.getParameter(paramName);
						
						if(RegexUtil.isInt(valstr)){
							kvMap.put(paramName, Integer.valueOf(valstr));
						}else{
							kvMap.put(paramName, valstr);
						}
						if(StringUtils.isBlank(valstr) && "acId".equals(paramName)){
							int acId = UserLoginUtil.getAcId();
							kvMap.put(paramName, acId);
							valstr = acId + "";
						}
						if(StringUtils.isBlank(valstr)){
							//如果获取到的parameter为空，那么从Attribute中获取
							Object valObj = request.getAttribute(paramName);
							if(valObj == null && mav != null && mav.getModel() != null){
								valObj = mav.getModel().get(paramName);
							}
							if(valObj != null){
								kvMap.put(paramName, valObj);
								valstr = parseStr(valObj);
							}
						}
						if(StringUtils.isNotBlank(valstr)){
							builder.append(paramName + "=" + valstr + "&");
						}
					}
					if(builder.length() > 0){
						builder.setLength(builder.length() - 1); //去掉末尾的 & 符号
						
						queryStr = builder.toString();
					}
				}
				
				String key = null;
				if(!(loader instanceof ContextHeaderLoader)){//ContextHeadLoader 加载的标题信息，无需缓存
					key = servletUri + (queryStr == null ? "" : "?" + queryStr);
					header = getCache(key);
					LOG.info("get currCacheUri:" + key + ",title:" + (header == null ? "" : header.getTitle()));
					if(header != null){
						return header;
					}
					
				}
				
				header = loader.loadHeader(kvMap);
				if(header != null && key != null){
					LOG.info("set currCacheUri:" + key + ",title:" + (header == null ? "" : header.getTitle()));
					setCache(key, header);
				}
			}
		}
		
		return header;
	}
	
	/**
	 * 如果obj是基本类型或者String，泽返回其字符串形式的值；如果为BaseEntity，则返回其ID；否则返回null
	 * @param obj
	 * @return
	 */
	private static String parseStr(Object obj){
		if(obj instanceof BaseEntity){
			BaseEntity entity = (BaseEntity)obj;
			return entity.getPK() == null ? null : entity.getPK().toString();
		}
		if(obj instanceof String || obj instanceof Integer || obj instanceof Long || obj instanceof Boolean || obj instanceof Byte || obj instanceof Short || obj instanceof Float || obj instanceof Double || obj instanceof Character){
			return obj.toString();
		}
		return null;
	}
	
	private static String parsePatternUri(String uri, List<String> uriParams){
		if(StringUtils.isBlank(uri)){
			return uri;
		}
		if(CollectionUtils.isEmpty(uriParams)){
			uriParams =  RegexUtil.getParamNames(uri);
		}
		
		if(!CollectionUtils.isEmpty(uriParams)){
			for(String paramName : uriParams){
				uri = RegexUtil.replaceParamName(uri, paramName, "*");
			}
		}
		
		return uri;
	}
	
	private static void loadHeader(CommonHeaderLoader loader, boolean isInit){
		CommonHeader header = loader.loadHeader(null);
		Set<String> acceptUris = loader.acceptUris();
		if(header == null && isInit){
			if(CollectionUtils.isEmpty(loader.getDynamicParamNames())){
				LOG.warn("not load any data for " + loader.acceptUris());
			}else if(!CollectionUtils.isEmpty(acceptUris)){
				for(String uri : acceptUris){
					List<String> uriParams = RegexUtil.getParamNames(uri);
					if(!CollectionUtils.isEmpty(uriParams)){
						loader.addDynamicParamNames(uriParams.toArray(new String[0]));
						uri = parsePatternUri(uri, uriParams);
						patternLoaderMap.put(uri, loader);
						patternUri2ParamNameMap.put(uri, uriParams);
					}else{
						loaderMap.put(uri, loader);
					}
				}
			}
			return;
		}
		
		if(header != null){
			for(String uri : acceptUris){
				if(isInit && loaderMap.containsKey(uri)){
					throw new BaseRuntimeException("ALREADY_EXIST", "该uri(" + uri + ")已存在！");
				}
				
				loaderMap.put(uri, loader);
				setCache(uri, header);
			}
		}
		
		/*if(isInit){
			int intervalMinutes =  loader.getRefreshTime();
			if(intervalMinutes > 0){
				TaskUtil.execFixedDelay(new HeaderTask(loader), 60, intervalMinutes, TimeUnit.MINUTES);
			}
		}*/
	}

	public List<CommonHeaderLoader> getLoaders() {
		return loaders;
	}


	public void setLoaders(List<CommonHeaderLoader> loaders) {
		this.loaders = loaders;
	}
	
	/**
	 * 
	 * @param loader 刷新某个标题头对应的标题
	 */
	/*public static void refreshHeader(CommonHeaderLoader loader){
		if(loader instanceof ContextHeaderLoader){
			return; //ContextHeaderLoader类的标题头信息因为没有缓存，所以无需重新刷新
		}
		Set<String> uris = loader.acceptUris();
		
		for(String cachedUri : cachedUris){
			for(String acptUri : uris){
				String patternUri = null;
				//判断缓存的uri是否和配置文件中loader指定的uri匹配，如果匹配，则删除缓存
				if(cachedUri.startsWith(acptUri) || 
						(!CollectionUtils.isEmpty(loader.getDynamicParamNames()) 
								&& (patternUri = parsePatternUri(acptUri, loader.getDynamicParamNames()))  != null
								&& urlMatcher.match(patternUri, cachedUri)
						)){
					delCache(cachedUri);
				}
			}
		}
		
		loadHeader(loader, false);
			
		LOG.debug(LOG.isDebugEnabled() ? "refresh common header for '"+ loader.acceptUris() + "' over." : null);
	}*/
	
    /*private class HeaderTask implements Runnable{
		
		private CommonHeaderLoader loader;
		
		public HeaderTask(CommonHeaderLoader loader) {
			if(loader == null){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "loader cannot be null");
			}
			this.loader = loader;
		}

		@Override
		public void run() {
			Set<String> uris = loader.acceptUris();
			
			for(String cachedUri : cachedUris){
				for(String acptUri : uris){
					String patternUri = null;
					//判断缓存的uri是否和配置文件中loader指定的uri匹配，如果匹配，则删除缓存
					if(cachedUri.startsWith(acptUri) || 
							(!CollectionUtils.isEmpty(loader.getDynamicParamNames()) 
									&& (patternUri = parsePatternUri(acptUri, loader.getDynamicParamNames()))  != null
									&& urlMatcher.match(patternUri, cachedUri)
							)){
						delCache(cachedUri);
					}
				}
			}
			
			for(CommonHeaderLoader loader : loaders){
				loadHeader(loader, false);
				
				LOG.debug(LOG.isDebugEnabled() ? "refresh common header for '"+ loader.acceptUris() + "' over." : null);
			}
		}
		
	}*/
	
}
