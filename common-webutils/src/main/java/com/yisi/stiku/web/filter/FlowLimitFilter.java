package com.yisi.stiku.web.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yisi.stiku.cache.constant.DefaultJedisKeyNS;
import com.yisi.stiku.cache.redis.ShardJedisTool;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;

/**
 * @author shangfeng
 *
 */
public class FlowLimitFilter implements Filter {

	private final static Logger LOG = LoggerFactory.getLogger(FlowLimitFilter.class);

	private final static String LOCAL_IP = IPUtil.getLocalIp();
	private final static String ZK_NODE_PATH = "common/flowlimit.properties";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		String redisKey = null;

		Iterator<String> keysItr = ConfigOnZk.getKeys(ZK_NODE_PATH, ZkConstant.ALIAS_PROJECT_NAME);
		if (keysItr != null) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String servletUri = httpRequest.getServletPath();
			String uriKey = ZkConstant.ALIAS_PROJECT_NAME + "." + servletUri;
			try {
				while (keysItr.hasNext()) {
					String key = keysItr.next();
					if (key.equals(uriKey)) {
						String configVal = ConfigOnZk.getValue(ZK_NODE_PATH, key);
						if (StringUtils.isNotBlank(configVal)) {
							redisKey = checkMaxFlow(configVal, servletUri);
						} else {
							LOG.warn("No flow config value for uri:" + servletUri + " in project "
									+ ZkConstant.ALIAS_PROJECT_NAME);
						}
					}
				}
			} catch (BaseRuntimeException e) {
				if (e.isException("TOO_MANY_REQUEST")) {
					throw e;
				} else {
					// 只有抛出了流量超限的异常
					LOG.warn(e.getMessage(), e);
				}
			}
		}

		try {
			chain.doFilter(request, response);
		} finally {
			// 請求處理完之後，需要將流量值減1
			if (StringUtils.isNotBlank(redisKey)) {
				ShardJedisTool.decr(DefaultJedisKeyNS.flowcrtl, redisKey);
			}
		}

	}

	private String checkMaxFlow(String configVal, String servletUri) {

		Map<String, Integer> flowConfigMap = new Gson().fromJson(configVal, new TypeToken<Map<String, Integer>>() {
		}.getType());
		Integer maxFlow = flowConfigMap.get("default");
		String redisKey = ZkConstant.ALIAS_PROJECT_NAME + servletUri;
		if (maxFlow == null) {
			maxFlow = flowConfigMap.get(LOCAL_IP);
			if (maxFlow != null) {
				redisKey += "-" + LOCAL_IP;
			}
		}
		long currFlow = ShardJedisTool.incr(DefaultJedisKeyNS.flowcrtl, redisKey);
		if (currFlow > maxFlow) {
			throw new BaseRuntimeException("TOO_MANY_REQUEST", "系统当前繁忙，请稍后再试！");
		}

		return redisKey;
	}

	@Override
	public void destroy() {

	}

}
