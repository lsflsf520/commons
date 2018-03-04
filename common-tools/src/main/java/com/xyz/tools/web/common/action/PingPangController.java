package com.xyz.tools.web.common.action;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyz.tools.cache.constant.DefaultCacheNS;
import com.xyz.tools.cache.eh.EhCacheTool;
import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.web.common.service.DataDictService;
import com.xyz.tools.web.util.WebUtils;

@Controller
public class PingPangController {
	
	@Resource
	private DataDictService dataDictService;

	@RequestMapping("/ping/pang")
	public void ping(HttpServletRequest request, HttpServletResponse response){
		WebUtils.writeJson("PONG", request, response);
	}
	
	
	@RequestMapping("/reloadconfig")
	@ResponseBody
	public ResultModel reloadConfig(){
		BaseConfig.init();
		
		return new ResultModel(true);
	}
	
	@RequestMapping("/getconfig")
	@ResponseBody
	public ResultModel getConfig(String key){
		
		return new ResultModel(BaseConfig.getValue(key));
	}
	
	@RequestMapping("loadDict")
	@ResponseBody
	public Object loadDict(String key){
		return dataDictService.getKVMap(key);
	}
	
	@RequestMapping("flushDict")
	@ResponseBody
	public ResultModel flushDict(String key){
		if(StringUtils.isBlank(key)){
			dataDictService.refreshData();
		} else {
			dataDictService.refreshData(key);
		}
		
		return new ResultModel(true);
	}
	
	@RequestMapping("flushehcache")
	@ResponseBody
	public ResultModel flushEhcache(DefaultCacheNS cacheNS){
		EhCacheTool.removeAll(cacheNS);
		
		return new ResultModel(true);
	}
	
	@RequestMapping("loadehcache")
	@ResponseBody
	public Object loadEhCache(DefaultCacheNS cacheNS, String key){
		
		return EhCacheTool.getValue(cacheNS, StringUtils.isBlank(key) ? "" : key.trim());
	}
	
	@RequestMapping("removeCookies")
	@ResponseBody
	public ResultModel removeCookies(HttpServletRequest request, HttpServletResponse response){
		
		WebUtils.deleteAllCookies(request, response);
		
		return new ResultModel(true);
	}
}
