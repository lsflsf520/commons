package com.xyz.tools.web.common.service.dictloader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StaticDataDictLoader extends DataDictLoader{

	private Map<String, Serializable> dataMap;
	protected String realNs; //数据实际来源，比如是某张表的某个字段，也可以是其它某个自定义的名称

	@Override
	public Map<String, Serializable> loadData() {
		return dataMap == null ? new HashMap<String, Serializable>() : dataMap;
	}

	public void setDataMap(Map<String, Serializable> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public String getRealNs() {
		return realNs;
	}

	public void setRealNs(String realNs) {
		this.realNs = realNs;
	}

	@Override
	public void execute() {
	}
	
}
