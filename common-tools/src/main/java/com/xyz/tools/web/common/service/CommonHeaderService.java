package com.xyz.tools.web.common.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.xyz.tools.cache.constant.DefaultJedisKeyNS;
import com.xyz.tools.cache.redis.SpringJedisTool;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RegexUtil;
import com.xyz.tools.db.bean.BaseEntity;
import com.xyz.tools.web.common.service.CommonHeaderLoader.CommonHeader;
import com.xyz.tools.web.common.service.headloader.ContextHeaderLoader;

/**
 * 
 * @author lsf
 *
 */
public class CommonHeaderService implements InitializingBean, ApplicationContextAware {

	private final static Logger LOG = LoggerFactory.getLogger(CommonHeaderService.class);

	private List<CommonHeaderLoader> loaders;
	private SpringJedisTool springJedisTool;

	private final static Map<String/* acceptUri */, CommonHeaderLoader> loaderMap = new HashMap<>();
	private final static Map<String/* acceptUri pattern */, CommonHeaderLoader> patternLoaderMap = new HashMap<>();
	private final static Map<String/* acceptUri pattern */, List<String>/* ordered path param names */> patternUri2ParamNameMap = new HashMap<>();

	// private final static Set<String> cachedUris = new ConcurrentHashSet<>();

	private static AntPathMatcher urlMatcher = new AntPathMatcher();

	@Override
	public void afterPropertiesSet() throws Exception {
		if (loaders != null) {
			for (CommonHeaderLoader loader : loaders) {
				loadHeader(loader, true);
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		try {
			springJedisTool = context.getBean(SpringJedisTool.class);
		} catch (Exception e) {
			LogUtils.warn("not found bean for SpringJedisTool in ApplicationContext");
		}
	}

	/**
	 * 直接删除完全匹配的uri缓存
	 * 
	 * @param uri
	 */
	public void delExactUri(String uri) {
		springJedisTool.del(DefaultJedisKeyNS.common_header, uri);
	}

	/**
	 * 解析servletUri中匹配到的参数，并设置到request的Attribute中，便于后续调用
	 * 
	 * @param request
	 * @param servletUri
	 * @param patternUri
	 *            能匹配到servletUri的 uri 模式，该模式从spring的配置属性中分析获得，参见
	 *            loadHeader(CommonHeaderLoader loader, boolean isInit) 方法
	 */
	private void parsePathParams(HttpServletRequest request, String servletUri, String patternUri) {
		List<String> pathParamNames = patternUri2ParamNameMap.get(patternUri);
		if (!CollectionUtils.isEmpty(pathParamNames)) {
			String regex = patternUri.replace("*", "([^\\.]+)");
			List<String> vals = RegexUtil.extractGroups(regex, servletUri);
			if (!CollectionUtils.isEmpty(vals) && vals.size() == pathParamNames.size()) {
				for (int index = 0; index < pathParamNames.size(); index++) {
					request.setAttribute(pathParamNames.get(index), vals.get(index));
				}
			}
		}
	}

	private CommonHeader getCache(String uri) {
		String value = springJedisTool.get(DefaultJedisKeyNS.common_header, uri);
		if (StringUtils.isNotBlank(value) && !"{}".equals(value)) {
			CommonHeader header = new Gson().fromJson(value, CommonHeader.class);
			if (header != null && !header.isEmpty()) {
				return header;
			}
		}
		return null;
	}

	private void setCache(String uri, CommonHeader commHeader) {
		if (commHeader == null || commHeader.isEmpty()) {
			return;
		}
		springJedisTool.set(DefaultJedisKeyNS.common_header, uri, new Gson().toJson(commHeader));
		// cachedUris.add(uri);
	}

	/*
	 * private void delCache(String uri) { if (StringUtils.isBlank(uri)) { return; }
	 * 
	 * springJedisTool.del(DefaultJedisKeyNS.common_header, uri); }
	 */
	public CommonHeader getHeader(HttpServletRequest request, ModelAndView mav) {
		String servletUri = request.getServletPath();
		String qstr = request.getQueryString();
		CommonHeaderLoader loader = loaderMap.get(servletUri);
		CommonHeader header = null;
		if (!(loader instanceof ContextHeaderLoader)) {// ContextHeadLoader 加载的标题信息，无需缓存
			header = getCache(servletUri);
			if (header == null && StringUtils.isNotBlank(qstr)) {
				header = getCache(servletUri + "?" + qstr);
			}
		}
		if (header == null && request != null) {
			if (loader == null) {// 假如没有直接匹配到的uri，则从模式匹配的uri patternLoaderMap中查看是否有匹配到的uri
				for (String patternUri : patternLoaderMap.keySet()) {
					if (urlMatcher.match(patternUri, servletUri)) {
						loader = patternLoaderMap.get(patternUri);
						parsePathParams(request, servletUri, patternUri);
						break;
					}
				}
			}

			if (loader != null) {
				List<String> paramNames = null;
				Map<String, Object> kvMap = new HashMap<>();
				String queryStr = null;
				if ((paramNames = loader.getDynamicParamNames()) != null) {
					StringBuilder builder = new StringBuilder();
					Collections.sort(paramNames);// 这里排序是为了后边为对应的数据生成缓存的key
					for (String paramName : paramNames) {
						String valstr = request.getParameter(paramName);

						if (RegexUtil.isInt(valstr)) {
							kvMap.put(paramName, Integer.valueOf(valstr));
						} else {
							kvMap.put(paramName, valstr);
						}
						if (StringUtils.isBlank(valstr)) {
							// 如果获取到的parameter为空，那么从Attribute中获取
							Object valObj = request.getAttribute(paramName);
							if (valObj == null && mav != null && mav.getModel() != null) {
								valObj = mav.getModel().get(paramName);
							}
							if (valObj != null) {
								kvMap.put(paramName, valObj);
								valstr = parseStr(valObj);
							}
						}
						if (StringUtils.isNotBlank(valstr)) {
							builder.append(paramName + "=" + valstr + "&");
						}
					}
					if (builder.length() > 0) {
						builder.setLength(builder.length() - 1); // 去掉末尾的 & 符号

						queryStr = builder.toString();
					}
				}

				String key = null;
				if (!(loader instanceof ContextHeaderLoader)) {// ContextHeadLoader 加载的标题信息，无需缓存
					key = servletUri + (queryStr == null ? "" : "?" + queryStr);
					header = getCache(key);
					LOG.info("get currCacheUri:" + key + ",title:" + (header == null ? "" : header.getTitle()));
					if (header != null) {
						return header;
					}

				}

				header = loader.loadHeader(kvMap);
				if (header != null && key != null) {
					LOG.info("set currCacheUri:" + key + ",title:" + (header == null ? "" : header.getTitle()));
					setCache(key, header);
				}
			}
		}

		return header;
	}

	/**
	 * 如果obj是基本类型或者String，泽返回其字符串形式的值；如果为BaseEntity，则返回其ID；否则返回null
	 * 
	 * @param obj
	 * @return
	 */
	private String parseStr(Object obj) {
		if (obj instanceof BaseEntity) {
			BaseEntity entity = (BaseEntity) obj;
			return entity.getPK() == null ? null : entity.getPK().toString();
		}
		if (obj instanceof String || obj instanceof Integer || obj instanceof Long || obj instanceof Boolean
				|| obj instanceof Byte || obj instanceof Short || obj instanceof Float || obj instanceof Double
				|| obj instanceof Character) {
			return obj.toString();
		}
		return null;
	}

	private String parsePatternUri(String uri, List<String> uriParams) {
		if (StringUtils.isBlank(uri)) {
			return uri;
		}
		if (CollectionUtils.isEmpty(uriParams)) {
			uriParams = RegexUtil.getParamNames(uri);
		}

		if (!CollectionUtils.isEmpty(uriParams)) {
			for (String paramName : uriParams) {
				uri = RegexUtil.replaceParamName(uri, paramName, "*");
			}
		}

		return uri;
	}

	private void loadHeader(CommonHeaderLoader loader, boolean isInit) {
		CommonHeader header = loader.loadHeader(null);
		Set<String> acceptUris = loader.acceptUris();
		if (header == null && isInit) {
			if (CollectionUtils.isEmpty(loader.getDynamicParamNames())) {
				LOG.warn("not load any data for " + loader.acceptUris());
			} else if (!CollectionUtils.isEmpty(acceptUris)) {
				for (String uri : acceptUris) {
					List<String> uriParams = RegexUtil.getParamNames(uri);
					if (!CollectionUtils.isEmpty(uriParams)) {
						loader.addDynamicParamNames(uriParams.toArray(new String[0]));
						uri = parsePatternUri(uri, uriParams);
						patternLoaderMap.put(uri, loader);
						patternUri2ParamNameMap.put(uri, uriParams);
					} else {
						loaderMap.put(uri, loader);
					}
				}
			}
			return;
		}

		if (header != null) {
			for (String uri : acceptUris) {
				if (isInit && loaderMap.containsKey(uri)) {
					throw new BaseRuntimeException("ALREADY_EXIST", "该uri(" + uri + ")已存在！");
				}

				loaderMap.put(uri, loader);
				setCache(uri, header);
			}
		}

	}

	public List<CommonHeaderLoader> getLoaders() {
		return loaders;
	}

	public void setLoaders(List<CommonHeaderLoader> loaders) {
		this.loaders = loaders;
	}

}
