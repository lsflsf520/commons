package com.ujigu.secure.web.common.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Message;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.TaskUtil;
import com.ujigu.secure.mq.bean.ModifyMsg;
import com.ujigu.secure.mq.receiver.topic.DbMsgTopicHandler;
import com.ujigu.secure.web.common.service.dictloader.DataDictLoader;
import com.ujigu.secure.web.common.service.dictloader.TableDataDictLoader;


public class DataDictService implements InitializingBean, DbMsgTopicHandler{
	private final static Logger LOG = LoggerFactory.getLogger(DataDictService.class);
	
	private List<DataDictLoader> loaders;
	
	private final static Map<String/*realNs*/, Map<String/*key*/, Serializable/*value*/>> dataMap = new HashMap<>();
	private final static Map<String/*nsAlias*/, String /*realNs*/> alias2NsMap = new HashMap<>();
	private final static Map<String/*table name*/, Set<String /*nsAlias*/>> table2aliasMap = new HashMap<>();
	
	private Map<String, DataDictLoader> loaderMap = new HashMap<>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(loaders != null){
			for(DataDictLoader loader : loaders){
				loadDict(loader, true);
			}
		}
		
	}
	
	/**
	 * 
	 * @param loader
	 * @param isInit 是否是第一次加载
	 */
	private void loadDict(DataDictLoader loader, boolean isInit){
		Map<String, Serializable> kvMap = loader.loadData();
		if(kvMap != null){
			String realNs = loader.getRealNs();
			if(isInit && dataMap.containsKey(realNs)){
				throw new BaseRuntimeException("DATA_EXIST", "数据已存在！realNs:" + realNs);
			}
			dataMap.put(realNs, kvMap);
			
			if(isInit){
				String aliasNs = loader.getNsAlias();
				if(StringUtils.isNotBlank(aliasNs)){
					if(alias2NsMap.containsKey(aliasNs)){
						throw new BaseRuntimeException("DATA_EXIST", "数据已存在！aliasNs:" + aliasNs);
					}
					alias2NsMap.put(aliasNs, realNs);
					
					if(loader instanceof TableDataDictLoader){
						TableDataDictLoader tblLoader = (TableDataDictLoader)loader;
						Set<String> aliasNss = table2aliasMap.get(tblLoader.getTableName());
						if(aliasNss == null){
							aliasNss = new HashSet<>();
							table2aliasMap.put(tblLoader.getTableName(), aliasNss);
						}
						aliasNss.add(aliasNs);
					}
				}
			}
		}
		
		if(isInit){
			TaskUtil.addTask(loader);
		}
//		int intervalMinutes =  loader.getRefreshTime();
//		if(intervalMinutes > 0){
//			TaskUtil.execFixedDelay(new DataDictTask(loader), 1, intervalMinutes, TimeUnit.MINUTES);
//		}
		
		String alias = StringUtils.isBlank(loader.getNsAlias()) ? loader.getRealNs() : loader.getNsAlias();
		loaderMap.put(alias, loader);
	}
	
	
	
	/**
	 * 
	 * @param aliasNs 刷新数据
	 */
	public void refreshData(String... aliasNs){
		Set<String> allAlias = alias2NsMap.keySet();
		if(aliasNs != null && aliasNs.length > 0){
			allAlias = new HashSet<>(Arrays.asList(aliasNs));
		} 
		
		for(String alias : allAlias){
			DataDictLoader loader = loaderMap.get(alias);
			if(loader != null){
				LogUtils.info("refreshData for alias %s", alias);
				loadDict(loader, false);
			}
		}
	}
	
	/**
	 * 
	 * @param namespace spring-dict.xml中配置的nsAlias，如果namespace对应的值是一个Map，那么可以使用 namespace.fieldName的形式来获取fieldName对应的值
	 * @param key 一个或多个字段拼接起来的key，与spring-dict.xml中配置的keyName顺序要保持一致
	 * @return
	 */
	public String getValue(String namespace, String key){
		namespace = namespace.toLowerCase();
		String fieldName = null;
		if(namespace.contains(".")){ //如果key是个 xx.yy 的形式，则说明数据字典对应
			String[] parts = namespace.split("\\.");
			if(parts.length != 2){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "namespace:" + namespace + " is illegal. right example: xx or xx.yy");
			}
			namespace = parts[0];
			fieldName = parts[1];
		}
		Map<String, Serializable> kvMap = dataMap.get(namespace);
		if(kvMap != null){
			Serializable valObj = kvMap.get(key);
			if(valObj == null){
				return "";
			}
			
			if(StringUtils.isNotBlank(fieldName)){
				if(valObj instanceof Map){
					Map<String, Serializable> valMap = (Map<String, Serializable>)valObj;
					valObj = valMap.get(fieldName);
				} else {
					throw new BaseRuntimeException("ILLEGAL_DATA", "value for key:" + key + " is not a Map.");
				}
			}
			
			return  valObj == null ? "" : valObj.toString();
		}
		
		String realNs = alias2NsMap.get(namespace);
		if(StringUtils.isNotBlank(realNs)){
			if(StringUtils.isNotBlank(fieldName)){
				realNs = realNs + "." + fieldName;
			}
			return getValue(realNs, key);
		}
		
		return "";
	}
	
	/**
	 * 根据给定的参数和规则组装成一个key
	 * @param args
	 * @return
	 */
	public String buildKey(String... args){
		String key = "";
		if(args != null && args.length > 0){
		    for(String arg : args){
			    key += arg + GlobalConstant.SQL_FIELD_SPLITER;
		    }
		    key = key.substring(0, key.length() - GlobalConstant.SQL_FIELD_SPLITER.length()); //去掉末尾的分隔符
		}
		return key;
	}
	
	/**
	 * 
	 * @param namespace spring-dict.xml中配置的nsAlias，如果namespace对应的值是一个Map，那么可以使用 namespace.fieldName的形式来获取fieldName对应的值
	 * @return
	 */
	public Map<String, Serializable> getKVMap(String namespace){
		namespace = namespace.toLowerCase();
		String fieldName = null;
		if(namespace.contains(".")){ //如果key是个 xx.yy 的形式，则说明数据字典对应
			String[] parts = namespace.split("\\.");
			if(parts.length != 2){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "namespace:" + namespace + " is illegal. right example: xx or xx.yy");
			}
			namespace = parts[0];
			fieldName = parts[1];
		}
		Map<String, Serializable> kvMap = dataMap.get(namespace);
		if(kvMap != null){
			Map<String, Serializable> finalKvMap = new LinkedHashMap<>();
			for(String key : kvMap.keySet()){
				Serializable valObj = kvMap.get(key);
				if(StringUtils.isNotBlank(fieldName)){
					if(valObj instanceof Map){
						Map<String, Serializable> valMap = (Map<String, Serializable>)valObj;
						valObj = valMap.get(fieldName);
					} else {
						throw new BaseRuntimeException("ILLEGAL_DATA", "value for key:" + key + " is not a Map.");
					}
				}
				
				finalKvMap.put(key, valObj);
			}
			
			return finalKvMap;
		}
		
		String realNs = alias2NsMap.get(namespace);
		if(StringUtils.isNotBlank(realNs)){
			if(StringUtils.isNotBlank(fieldName)){
				realNs = realNs + "." + fieldName;
			}
			return getKVMap(realNs);
		}
		
		return Collections.emptyMap();
	}
	
	/**
	 * 
	 * @param namespace spring-dict.xml中配置的nsAlias，如果namespace对应的值是一个Map，那么可以使用 namespace.fieldName的形式来获取fieldName对应的值
	 * @param suffix 字典值中的key的后缀，返回的Map的key中，会剔除掉suffix。比如 有如下数据字典：
	 *                 baseInfo -> name,company -> value
	 *           如果指定的namespace为baseInfo， suffix为company, 那么返回的Map就是   name -> value
	 * @return
	 */
	public Map<String, Serializable> getKVMap(String namespace, String suffix){
		Map<String, Serializable> kvMap = getKVMap(namespace);
		if(StringUtils.isNotBlank(suffix)){
			if(!suffix.startsWith(GlobalConstant.SQL_FIELD_SPLITER)){
				suffix = GlobalConstant.SQL_FIELD_SPLITER + suffix;
			}
			
			Map<String, Serializable> filteredMap = new LinkedHashMap<String, Serializable>();
			for(String k : kvMap.keySet()){
				if(k.endsWith(suffix)){
					filteredMap.put(k.replace(suffix, ""), kvMap.get(k));
				}
			}
			
			return filteredMap;
		}
		
		return kvMap;
	}

	public void setLoaders(List<DataDictLoader> loaders) {
		this.loaders = loaders;
	}
	
	/**
	 * 重写实际命名空间的字典数据
	 * @param realNs
	 * @param kvMap
	 */
	public static void rewriteDict(String realNs, Map<String, Serializable> kvMap){
		if(dataMap.containsKey(realNs) && kvMap != null){
			dataMap.put(realNs, kvMap);
			
			LOG.debug(LOG.isDebugEnabled() ? "refresh data for " + realNs + " with size " + (kvMap == null ? 0 : kvMap.size()) : null);
		}else{
			LOG.warn("realNs " + realNs + " not exist in dataMap or param kvMap is null");
		}
	}

	@Override
	public void handleMsg(Message message, ModifyMsg modifyMsg) {
		Set<String> aliasNss = table2aliasMap.get(modifyMsg.getTableName());
		if(aliasNss != null){
			for(String aliasNs : aliasNss){
				refreshData(aliasNs);
			}
		}
	}

	@Override
	public String supportTables() {
		return StringUtils.join(table2aliasMap.keySet(), ",");
	}

	/*private class DataDictTask implements Runnable{
		
		private DataDictLoader loader;
		
		public DataDictTask(DataDictLoader loader) {
			if(loader == null){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "loader cannot be null");
			}
			this.loader = loader;
		}

		@Override
		public void run() {
			Map<String, String> kvMap = loader.loadData();
			if(kvMap != null){
				String realNs = loader.getRealNs();
				dataMap.put(realNs, kvMap);
			}
			LOG.debug(LOG.isDebugEnabled() ? "refresh data for " + loader.getRealNs() + " with size " + (kvMap == null ? 0 : kvMap.size()) : null);
		}
		
	}*/
	
}
