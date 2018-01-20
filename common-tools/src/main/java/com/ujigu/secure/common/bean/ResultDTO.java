package com.ujigu.secure.common.bean;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.JsonUtil;

/**
 * 
 * @author 跟客户端协议好的传输对象
 *
 */
public class ResultDTO {

	/**
	 * 主业务处理结果
	 */
	private Object data;

	/**
	 * 在返回结果中，除了实际处理返回的model外，还可能需要一些其它的辅助参数(比如是否为无线请求，是否为可信的爬虫请求等)，
	 * 可以存入该extraInfoMap里边，随model一起返回给前
	 */
	@JsonIgnore
	private Map<String, String> extraInfoMap = new HashMap<String, String>();

	/**
	 * 返回的处理结果代码
	 */
	private int code = 0;
	
	private String msg = "操作成功";
	
	public ResultDTO(){
	}
	
	public ResultDTO(int code, String msg){
		setCode(code);
		
		this.msg = msg;
	}
	
	public ResultDTO(Object data) {

		if (data == null) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "ResultModel不支持NULL参数");
		}
		this.data = data;
		if ((this.data != null && !(this.data instanceof Boolean)) || (Boolean) (this.data)) {
			setCode(0);
		}
	}


	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
		if(isSuccess()){
			this.msg = "操作成功";
		}
	}
	
	@JsonIgnore
	public boolean isSuccess() {

		return this.code == 0;
	}

	/**
	 * 该方法只能在model的类型为Map或者值为空的时候才可以使用 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("all")
	public <T> T getValue(String key){
		if(this.data == null){
			return null;
		}
		if (!(data instanceof Map)) {
			throw new BaseRuntimeException("NOT_SUPPORT", "不支持的操作！");
		}
		
		Map<String, Object> modelMap = (Map<String, Object>) data;
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
		if(this.getData() == null){
			return null;
		}
		Gson gson = JsonUtil.create();
		return gson.fromJson(gson.toJson(this.getData()), type);
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
