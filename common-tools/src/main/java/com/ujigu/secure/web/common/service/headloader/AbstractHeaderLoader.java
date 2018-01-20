package com.ujigu.secure.web.common.service.headloader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.web.common.service.CommonHeaderLoader;

public abstract class AbstractHeaderLoader implements CommonHeaderLoader {
	
	protected Set<String> uris;
	protected List<String> paramNames;
	
//	protected int refreshTime; //刷新缓存的时间间隔，以分钟为单位

	@Override
	public List<String> getDynamicParamNames() {
		return this.paramNames;
	}
	
	@Override
	public void addDynamicParamNames(String... paramNames) {
		if(paramNames == null || paramNames.length <= 0){
			return ;
		}
		if(this.paramNames == null){
			this.paramNames = new ArrayList<>();
		}
		for(String pname : paramNames){
			if(StringUtils.isNotBlank(pname)){
				this.paramNames.add(pname.trim());
			}
		}
	}

	@Override
	public Set<String> acceptUris() {
		return uris == null ? new HashSet<String>() : uris;
	}
	
	/*@Override
	public void execute() {
		CommonHeaderService.refreshHeader(this);
	}
	
	@Override
	public long interval() {
		return refreshTime * 60;
	}
	
	@Override
	public long firstExecDelay() {
		return 1800;
	}
	
	@Override
	public String name() {
		Set<String> myuris = acceptUris();
		if(CollectionUtils.isEmpty(myuris)){
			return this.toString();
		}
		
		return myuris.iterator().next();
	}*/
	
	public void setUri(String uri) {
		if(uris == null){
			uris = new HashSet<>();
		}
		uris.add(uri);
	}

	public void setUris(Set<String> uris) {
		this.uris = uris;
	}
	
	/*public int getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}*/

	/**
	 * 此方法主要用于从spring做IOC注入用，其它情况下建议使用 addDynamicParamName
	 * @param paramNames
	 */
	/*public void setParamNames(List<String> paramNames){
		if(!CollectionUtils.isEmpty(paramNames)){
			addDynamicParamNames(paramNames.toArray(new String[0]));
		}
	}*/
	
	/**
	 * 
	 * @param paramNames
	 */
	public void setParamNames(String paramNames){
		if(StringUtils.isNotBlank(paramNames)){
			addDynamicParamNames(paramNames.split(","));
		}
	}
	
}
