package com.xyz.tools.common.bean;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.Gson;
import com.xyz.tools.common.constant.GlobalResultCode;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.strategy.ResultCodeSerializer;
import com.xyz.tools.common.utils.JsonUtil;

/**
 * 
 * @author lsf
 *
 */
public class ResultModel {

	/**
	 * 主业务处理结果
	 */
	private Object model = new HashMap<>();

	/**
	 * 在返回结果中，除了实际处理返回的model外，还可能需要一些其它的辅助参数(比如是否为无线请求，是否为可信的爬虫请求等)，
	 * 可以存入该extraInfoMap里边，随model一起返回给前
	 */
	private Map<String, String> extraInfoMap = new HashMap<String, String>();

	/**
	 * 返回的处理结果代码
	 */
	private String resultCode = "UNKNOWN_ERROR";
	
	private String resultMsg;

	/**
	 * (RPC调用不用考虑这个字段，只有在进行web交互的时候才需要这个)如果当前请求为同步请求，并且该请求处理失败的情况下，
	 * 如果有redirectUrl存在，则会跳往这个错误页
	 */
	private String redirectUrl;

	/**
	 * 在校验或者业务处理的过程中，有需要临时缓存起来的变量(即后续处理会用到的变量)，可以临时存入该pickParamMap中
	 */
	@JsonIgnore
	private Map<String, Object> pickParamMap = new HashMap<String, Object>();
	
	/**
	 * 无参构造函数是留给序列化器使用的，不要手动调用
	 */
	public ResultModel() {}

	public ResultModel(String code, String resultMsg) {
		this.resultCode = code;
		this.resultMsg = StringUtils.isNotBlank(resultMsg) ? resultMsg : "UNKNOWN ERROR";
	}

	public ResultModel(ResultCodeIntf resultCodeBean) {
		this(resultCodeBean.getCode(), resultCodeBean.getFriendlyMsg());
	}

	@SuppressWarnings("all")
	public ResultModel(Object model) {
		if (model == null) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "ResultModel不支持NULL参数");
		}
		
		if ((model != null && !(model instanceof Boolean)) || (Boolean) (model)) {
			this.resultCode = GlobalResultCode.SUCCESS.getCode();
			this.resultMsg = GlobalResultCode.SUCCESS.getFriendlyMsg();
		}
		
        if(model instanceof Number || model instanceof String || model instanceof Boolean){
			((HashMap)this.model).put("data", model);
		} else {
			this.model = model;
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
	
	public String getResultCode() {
		return resultCode;
	}
	
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	@JsonIgnore
	public boolean isSuccess() {

		return GlobalResultCode.SUCCESS.name().equals(resultCode);
	}

	public void putPickParam(String key, Object value) {

		pickParamMap.put(key, value);
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
	 * 该方法只能在model的类型为Map或者值为空的时候才可以使用 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("all")
	public <T> T getValue(String key){
		if(model == null){
			return null;
		}
		if (!(model instanceof Map)) {
			throw new BaseRuntimeException("NOT_SUPPORT", "不支持的操作！");
		}
		
		Map<String, Object> modelMap = (Map<String, Object>) model;
		return (T)modelMap.get(key);
	}
	
	
	public Integer getInt(String key){
        Object value = getValue(key);
		
		return value == null ? null : Double.valueOf(value.toString()).intValue();
	}
	
	public Boolean getBool(String key){
		Boolean bool = getValue(key);
		
		return bool == null ? false : bool;
	}
	
	/**
	 * 如果model代表一个bean的数据，可以调用此方法将其转为对应的bean
	 * @param type
	 * @return
	 */
	public <T> T convertModel2Bean(Type type){
		if(this.getModel() == null){
			return null;
		}
		Gson gson = JsonUtil.create();
		return gson.fromJson(gson.toJson(this.getModel()), type);
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
