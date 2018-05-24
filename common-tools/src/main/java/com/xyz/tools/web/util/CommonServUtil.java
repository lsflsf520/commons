package com.xyz.tools.web.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.constant.GlobalConstant;
import com.xyz.tools.common.utils.LogUtils;

public class CommonServUtil {
	
	public final static String COUNTRY = "country";
	public final static String PROVINCE = "province";
	public final static String CITY = "city";

	/*public static ResultModel sendMsg(String appId, String phone, String tmplId, String... params) {

		return sendMsg(null, appId, phone, tmplId, params);
	}*/

	public static ResultModel sendMsg(String module, String appId, String phone, String tmplId, String... params) {
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", StringUtils.isBlank(module) ? GlobalConstant.PROJECT_NAME : module.trim());
		formParams.put("appId", appId);
		formParams.put("phone", phone);
		formParams.put("tmplId", tmplId);
		if(params != null && params.length > 0){
			formParams.put("params", StringUtils.join(params, ","));
		}

		return RestClientUtil.doPost(GlobalConstant.BASE_SERVICE_DOMAIN + "/sms/send.do", formParams,
				ResultModel.class);
	}

	/**
	 * 发送模板邮件
	 * @param module 这个参数需要找相关负责人提供
	 * @param apiuser 这个参数需要找相关负责人提供
	 * @param tos
	 * @param title
	 * @param tmplId
	 * @param subMap
	 * @param sectionMap
	 * @return
	 */
	public static ResultModel sendTmplEmail(String module, String apiuser, List<String> tos, String title, String tmplId,
			Map<String, String> subMap, Map<String, String> sectionMap) {
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", StringUtils.isBlank(module) ? GlobalConstant.PROJECT_NAME : module.trim());
		formParams.put("apiuser", apiuser);
		formParams.put("tos", StringUtils.join(tos, ","));
		formParams.put("title", title);
		formParams.put("tmplId", tmplId);

		if (!CollectionUtils.isEmpty(subMap)) {
			for (Entry<String, String> entry : subMap.entrySet()) {
				formParams.put("subMap['%" + entry.getKey() + "%']", entry.getValue());
			}
		}

		if (!CollectionUtils.isEmpty(sectionMap)) {
			for (Entry<String, String> entry : sectionMap.entrySet()) {
				formParams.put("sectionMap['%" + entry.getKey() + "%']", entry.getValue());
			}
		}

		return RestClientUtil.doPost(GlobalConstant.BASE_SERVICE_DOMAIN + "/email/tmpl/send.do", formParams,
				ResultModel.class);
	}
	
	/**
	 * 发送内容邮件
	 * @param module 这个参数需要找相关负责人提供
	 * @param apiuser 这个参数需要找相关负责人提供
	 * @param tos
	 * @param title
	 * @param content 邮件内容，内容中的变量用 %param1%、%param2% 的形式
	 * @param subMap 
	 * @param sectionMap
	 * @return
	 */
	public static ResultModel sendContentEmail(String module, String apiuser, List<String> tos, String title, String content,
			Map<String, String> subMap, Map<String, String> sectionMap){
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", StringUtils.isBlank(module) ? GlobalConstant.PROJECT_NAME : module.trim());
		formParams.put("apiuser", apiuser);
		formParams.put("tos", StringUtils.join(tos, ","));
		formParams.put("title", title);
		formParams.put("content", content);

		if (!CollectionUtils.isEmpty(subMap)) {
			for (Entry<String, String> entry : subMap.entrySet()) {
				formParams.put("subMap['%" + entry.getKey() + "%']", entry.getValue());
			}
		}

		if (!CollectionUtils.isEmpty(sectionMap)) {
			for (Entry<String, String> entry : sectionMap.entrySet()) {
				formParams.put("sectionMap['%" + entry.getKey() + "%']", entry.getValue());
			}
		}
		
		return RestClientUtil.doPost(GlobalConstant.BASE_SERVICE_DOMAIN + "/email/content/send.do", formParams,
				ResultModel.class);
	}
	
	/**
	 * 根据IP获取地理信息
	 * 目前可以获取国家、省、市
	 * 国家：resultModel.getValue(CommonServUtil.COUNTRY)
	 * 省：resultModel.getValue(CommonServUtil.PROVINCE);
	 * 市：resultModel.getValue(CommonServUtil.CITY);
	 * @param ip
	 * @return
	 */
	public static ResultModel getIPAddr(String ip){
		ResultModel resultModel = RestClientUtil.doGet(GlobalConstant.BASE_SERVICE_DOMAIN + "/ip/s.do?ip={ip}", ResultModel.class, ip);
		
		return resultModel;
	}
	
	/**
	 * 根据IP返回国家
	 * @param ip
	 * @return
	 */
	public static String getCountry(String ip){
		return getAreaPart(ip, CommonServUtil.COUNTRY);
	}
	
	/**
	 * 根据IP返回省
	 * @param ip
	 * @return
	 */
	public static String getProvince(String ip){
		return getAreaPart(ip, CommonServUtil.PROVINCE);
	}
	
	/**
	 * 根据IP返回城市
	 * @param ip
	 * @return
	 */
	public static String getCity(String ip){
		return getAreaPart(ip, CommonServUtil.CITY);
	}
	
	private static String getAreaPart(String ip, String areaPart){
		try{
			ResultModel resultModel = getIPAddr(ip);
			
			return resultModel.getValue(areaPart);
		} catch (Exception e) {
			LogUtils.warn("get area for IP %s error", ip);
		}
		
		return null;
	}

}
