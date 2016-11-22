package com.yisi.stiku.rpc.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.RandomUtil;

public class RpcRequest implements Serializable {

	private static final long serialVersionUID = 2750646443189480771L;

	protected final String messageId;
	protected Class<?> apiClass;
	protected final String method;
	protected final Object[] parameters;
	protected Class<?>[] parameterTypes;
	protected final String serviceVersion;
	protected String apiClassName;

	private final Map<String, Object> extraInfoMap = new HashMap<String, Object>();

	/**
	 * 构造心跳请求
	 */
	public RpcRequest() {
		this.messageId = "PING";
		this.method = null;
		this.apiClass = null;
		this.serviceVersion = null;
		this.parameters = null;
		this.apiClassName = null;

	}

	public RpcRequest(final String apiClassName, final String serviceVersion,
			final String method, final Object... parameters) {
		messageId = (System.nanoTime() + RandomUtil.rand(100000)) + "";
		this.apiClassName = apiClassName;
		this.method = method;
		this.serviceVersion = serviceVersion;

		this.parameters = parameters;

	}

	public RpcRequest(final Class<?> apiClass, final String serviceVersion,
			final String method, final Object... parameters) {
		messageId = (System.nanoTime() + RandomUtil.rand(100000)) + "";
		this.apiClass = apiClass;
		this.method = method;
		this.serviceVersion = serviceVersion;

		this.parameters = parameters;

	}

	public String getMessageId() {
		return messageId;
	}

	public Class<?> getApiClass() {
		try {
			return apiClass != null ? apiClass
					: (StringUtils.isBlank(apiClassName) ? null : Class
							.forName(apiClassName));
		} catch (ClassNotFoundException e) {
			throw new BaseRuntimeException("CLASS_NOT_FOUND", e.getMessage(), e);
		}
	}

	public String getApiClassName() {
		return StringUtils.isNotBlank(apiClassName) ? apiClassName
				: (apiClass == null ? null : apiClass.getName());
	}

	public String getMethod() {
		return method;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void addExtraInfo(String key, Object val) {
		this.extraInfoMap.put(key, val);
	}

	@SuppressWarnings("all")
	public <T> T getExtraInfo(String key) {
		return (T) this.extraInfoMap.get(key);
	}

	public Map<String, Object> getExtraInfoMap() {
		return Collections.unmodifiableMap(extraInfoMap);
	}

	public void putAll(Map<String, Object> extraInfoMap) {
		this.extraInfoMap.putAll(extraInfoMap);
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
}
