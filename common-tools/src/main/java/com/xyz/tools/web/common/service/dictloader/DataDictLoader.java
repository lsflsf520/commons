package com.xyz.tools.web.common.service.dictloader;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.intf.ITask;

public abstract class DataDictLoader implements ITask{
	
	protected String nsAlias; //命名空间的别名
	protected int refreshTime; //刷新缓存的时间间隔，以分钟为单位

	abstract public Map<String, Serializable> loadData();

	abstract public String getRealNs();

	public String getNsAlias() {
		return nsAlias;
	}

	public void setNsAlias(String nsAlias) {
		if(StringUtils.isNotBlank(nsAlias)){
			this.nsAlias = nsAlias.trim().toLowerCase();
		}
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	@Override
	public long interval() {
		return refreshTime * 60;
	}
	
	@Override
	public Long firstExecDelay() {
		return 180l; //默认3分钟后第一次刷新
	}
	
	@Override
	public String name() {
		return getRealNs();
	}
}
