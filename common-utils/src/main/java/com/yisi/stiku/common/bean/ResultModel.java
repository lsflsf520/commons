package com.yisi.stiku.common.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.strategy.ResultCodeSerializer;

/**
 * 
 * @author lsf
 *
 */
public class ResultModel {

	/**
	 * 主业务处理结果
	 */
	private Object model;

	/**
	 * 在返回结果中，除了实际处理返回的model外，还可能需要一些其它的辅助参数(比如是否为无线请求，是否为可信的爬虫请求等)，
	 * 可以存入该extraInfoMap里边，随model一起返回给前
	 */
	private Map<String, String> extraInfoMap = new HashMap<String, String>();

	/**
	 * 返回的处理结果代码
	 */
	@JsonSerialize(using = ResultCodeSerializer.class)
	private ResultCodeIntf resultCode = GlobalResultCode.UNKNOWN_ERROR;

	private String resultMsg;

	/**
	 * (RPC调用不用考虑这个字段，只有在进行web交互的时候才需要这个)如果当前请求为同步请求，并且该请求处理失败的情况下，
	 * 如果有redirectUrl存在，则会跳往这个错误页
	 */
	@JsonIgnore
	private String redirectUrl;

	/**
	 * 在校验或者业务处理的过程中，有需要临时缓存起来的变量(即后续处理会用到的变量)，可以临时存入该pickParamMap中
	 */
	@JsonIgnore
	private Map<String, Object> pickParamMap = new HashMap<String, Object>();

	public ResultModel(String code, String resultMsg) {

		this(new ServiceResultCode(code, resultMsg));
	}

	public ResultModel(ResultCodeIntf resultCodeBean) {

		this.resultCode = resultCodeBean;
		this.resultMsg = this.getResultMsg();
	}

	public ResultModel(Object model) {

		if (model == null) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "ResultModel不支持NULL参数");
		}
		this.model = model;
		if ((this.model != null && !(this.model instanceof Boolean)) || (Boolean) (this.model)) {
			this.resultCode = GlobalResultCode.SUCCESS;
			this.resultMsg = this.getResultMsg();
		}
	}

	public static ResultModel buildMapResultModel() {

		return new ResultModel(new HashMap<String, Object>());
	}

	@SuppressWarnings("unchecked")
	public <T> T getModel() {

		return (T) model;
	}

	public void setModel(Object model) {

		this.model = model;
	}

	public String getRedirectUrl() {

		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {

		this.redirectUrl = redirectUrl;
	}

	public ResultCodeIntf getResultCode() {

		return resultCode;
	}

	public void setResultCode(ResultCodeIntf resultCode) {

		this.resultCode = resultCode;
	}

	public void setResultCode(String code, String errorMsg) {

		this.resultCode = new ServiceResultCode(code, errorMsg);
	}

	@JsonIgnore
	public boolean isSuccess() {

		return GlobalResultCode.SUCCESS.equals(resultCode);
	}

	public void putPickParam(String key, Object value) {

		pickParamMap.put(key, value);
	}

	public String getResultMsg() {

		return StringUtils.isNotBlank(resultMsg) ? resultMsg : (this.resultCode == null ? "UNKNOWN ERROR"
				: this.resultCode.getFriendlyMsg());
	}

	/**
	 * 该方法只能在model的类型为Map或者值为空的时候才可以使用 作用是将key、value作为键值对存储到 model属性中
	 * 
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("all")
	public ResultModel put(String key, Object value) {

		if (model != null && !(model instanceof Map)) {
			throw new BaseRuntimeException("NOT_SUPPORT", "不支持的操作！");
		}

		Map<String, Object> modelMap = (Map<String, Object>) model;
		if (model == null) {
			modelMap = new HashMap<String, Object>();
			model = modelMap;
		}

		modelMap.put(key, value);

		return this;
	}

	/**
	 * 
	 * @return 返回一个只读的Map
	 */
	@JsonIgnore
	public Map<String, Object> getPickParams() {

		return Collections.unmodifiableMap(pickParamMap);
	}

	@SuppressWarnings("unchecked")
	public <T> T getPickParam(String key) {

		return (T) pickParamMap.get(key);
	}

	public void clearPickParam() {

		pickParamMap.clear();
	}

	public void addExtraInfo(String key, String value) {

		extraInfoMap.put(key, value);
	}

	public String getExtraInfo(String key) {

		return extraInfoMap.get(key);
	}

	public Map<String, String> getExtraInfoMap() {

		return Collections.unmodifiableMap(extraInfoMap);
	}

}
