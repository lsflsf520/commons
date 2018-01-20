package com.ujigu.secure.web.common.service.headloader;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.db.service.impl.DBDataUtil;

public class TableHeaderLoader extends AbstractHeaderLoader{
	
	private final static Logger LOG = LoggerFactory.getLogger(TableHeaderLoader.class);
	
	private DataSource dataSource;
	private String sql;
	
	private CommonHeader loadHeader(String sql){
		
		List<Map<String/* fieldName */, Serializable/* value */>> lineDatas = DBDataUtil.loadData(dataSource, sql);
		if(CollectionUtils.isEmpty(lineDatas)){
			return null;
		}
		Map<String, Serializable> lineData = lineDatas.get(0);
		String title = lineData.get("title") == null ? null : lineData.get("title").toString();
		String kword = lineData.get("kword") == null ? null : lineData.get("kword").toString();
		String descp = lineData.get("descp") == null ? null : lineData.get("descp").toString();
				
		CommonHeader header = new CommonHeader(title, kword, descp);
	    
		return header;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	@Override
	public CommonHeader loadHeader(Map<String, Object> paramMap) {
		if(StringUtils.isBlank(sql)){
			LOG.warn("sql not set yet for uris " + uris);
			return null;
		}
		String currSql = sql;
		if(CollectionUtils.isEmpty(this.paramNames)){
			this.paramNames = RegexUtil.getParamNames(currSql); //如果sql中有需要动态替换的参数名，则不再继续执行数据加载
		}
		
		if(!CollectionUtils.isEmpty(this.paramNames)){
			if(paramMap == null || CollectionUtils.isEmpty(paramMap)){
				LOG.debug("exec the sql need provide params " + this.paramNames);
				return null;
			}
			for(String paramName : this.paramNames){
				Object value = paramMap.get(paramName);
				currSql = RegexUtil.replaceParamName(currSql, paramName, value instanceof Integer ? value : "'" + value.toString()+ "'");
			}
		}
		
		LogUtils.debug("header from table data sql:%s, paramMap:%s", currSql, paramMap);
		return loadHeader(currSql);
	}
	
}
