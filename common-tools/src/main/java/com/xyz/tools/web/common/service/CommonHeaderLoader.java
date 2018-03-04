package com.xyz.tools.web.common.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author lsf
 *
 */
public interface CommonHeaderLoader {

	/**
	 * 根据当前的请求，获取getDynamicParamNames参数来动态执行数据加载
	 * @param request
	 * @return
	 */
	CommonHeader loadHeader(Map<String, Object> paramMap);
	
	/**
	 * 获取动态参数名
	 * @return
	 */
	List<String> getDynamicParamNames();
	
	/**
	 * 添加动态参数名
	 * @param paramName
	 */
	void addDynamicParamNames(String... paramNames);
	
	/**
	 * 刷新缓存的时间间隔，以分钟为单位
	 * @return
	 */
//	public int getRefreshTime();
	
	/**
	 * 
	 * @return 返能使用本页头数据的uri列表
	 */
	Set<String> acceptUris();
	
	public static class CommonHeader{
		private String title = null;
		private String kword = null;
		private String desc = null;
		
		public CommonHeader(String title, String kword, String desc){
			this.title = title;
			this.kword = kword;
			this.desc = desc;
		}
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getKword() {
			return kword;
		}
		public void setKword(String kword) {
			this.kword = kword;
		}
		public String getDesc() {
			return desc;
		}
		public void setDesc(String desc) {
			this.desc = desc;
		}
		
		public boolean isEmpty(){
			return StringUtils.isBlank(title) && StringUtils.isBlank(kword) && StringUtils.isBlank(desc);
		}
	}
	
}
