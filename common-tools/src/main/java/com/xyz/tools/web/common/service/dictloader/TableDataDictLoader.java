package com.xyz.tools.web.common.service.dictloader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.xyz.tools.common.constant.GlobalConstant;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.db.utils.DBDataUtil;
import com.xyz.tools.web.common.service.DataDictService;

public class TableDataDictLoader extends DataDictLoader {
	
    private final static Logger LOG = LoggerFactory.getLogger(TableDataDictLoader.class);
	
	private DataSource dataSource;
	private String tableName; //表名 或者 数据库名+表名组合而成
//	private String nsAlias; //命名空间的别名
	private String keyName; //作为key的列名
	private String valueName; //作为值的列名
	private String condSql; //不用包含 where 关键词
	private boolean lazyLoad; //是否开启懒加载模式，建议大表都开启懒加载模式
	private ValueConverter converter;
	
	@Override
	public void execute() {
		Map<String, Serializable> dataMap = loadData();
		
		DataDictService.rewriteDict(getRealNs(), dataMap);
	}

	@Override
	public Map<String, Serializable> loadData() {
		if(this.isLazyLoad()){ //如果是懒加载模式，此方法将直接返回一个空的Map，让程序在需要使用数据的时候再加载数据
			return new HashMap<String, Serializable>();
		}
		return loadData(this.condSql);
	}
	
	/**
	 * 开启懒加载模式的loader会调用此方法
	 * @param keys
	 * @return
	 */
	public Map<String, Serializable> lazyLoadData(Serializable... keys){
		String currCondSql = this.condSql;
		String[] keyColNameArr = null;
		if(keys != null){
			if(keys.length != (keyColNameArr = keyName.split(",")).length) {
				throw new BaseRuntimeException("PARAM_NUM_NOT_MATCH", "参数个数与keyName(" + keyName + "个数不一致)");
			}
			String otherCondStr = "";
			for(int index = 0; index < keyColNameArr.length; index++) {
				otherCondStr += keyColNameArr[index] + "=" + parseColumnValue(keys[index]);
				if(index < keyColNameArr.length - 1){
					otherCondStr += " and ";
				}
			}
			
			currCondSql = otherCondStr + (StringUtils.isBlank(currCondSql) ? "" : " and " + currCondSql);
		}
		
		return loadData(currCondSql);
	}
	
	private Map<String, Serializable> loadData(String condSql) {
        Map<String, Serializable> dataMap = new LinkedHashMap<>();
		
		String whereSql = (StringUtils.isBlank(condSql) ? "" : " where " + condSql);
		List<Map<String/* fieldName */, Serializable/* value */>> dbDatas = DBDataUtil.loadData(dataSource, "select " + keyName + "," + valueName +" from " + tableName + whereSql);
		if(!CollectionUtils.isEmpty(dbDatas)){
			String[] keyColNames = keyName.split(",");
			String[] valColNames = valueName.split(",\\s+"); //如果列是用函数来处理的，函数中不能有任何空格，因为获取列名的时候是以 英文逗号+空格 的方式来切分或者列的别名的
			for(int index = 0; index < valColNames.length; index++){
				String valparts[] = valColNames[index].split("\\s+"); 
				if(valparts.length > 0){ //获取列的别名
					valColNames[index] = valparts[valparts.length - 1].trim();
				}
			}
			for(Map<String/* fieldName */, Serializable/* value */> lineData : dbDatas){
				try{
					String key = "";
					if(keyColNames.length > 1){
						for(String part : keyColNames){
							Serializable partVal = lineData.get(part.trim());
							if(partVal == null){
								throw new BaseRuntimeException("ILLEGAL_VAL", "val is null for column " + part + " in table " + tableName);
							}
							key += partVal + GlobalConstant.SQL_FIELD_SPLITER;
						}
						key = key.substring(0, key.length() - GlobalConstant.SQL_FIELD_SPLITER.length()); //去掉末尾的分隔符
					}else{
						key = lineData.get(keyName) == null ? "" : lineData.get(keyName).toString();
						if(StringUtils.isBlank(key)){
							throw new BaseRuntimeException("ILLEGAL_VAL", "val is null for column " + keyName + " in table " + tableName);
						}
					}
					LinkedHashMap<String, Serializable> valMap = new LinkedHashMap<>();
					if(valColNames.length == 1){
						Serializable value = lineData.get(valColNames[0]) == null ? "" : lineData.get(valColNames[0]).toString();
						valMap.put(valColNames[0], value);
					} else {
						for(String valName : valColNames){
							valMap.put(valName, lineData.get(valName));
						}
					}
					//对值进行加工
					if(converter != null){
						converter.convertData(valMap);
					}
					
					//假如加工处理后的Map值中，只有一条数据，则直接存储为单行值，如果有多行，则存储成LinkedHashMap，所以要确保加工方法中对同一类型的数据处理后，Map中的值数量是一致的，否则可能导致不可预知的问题
					if(valMap.size() == 1){
						dataMap.put(key, valMap.values().iterator().next());
					} else {
						dataMap.put(key, valMap);
					}
				} catch (BaseRuntimeException e) {
					LOG.warn(e.getErrorCode() + "," + e.getFriendlyMsg());
				}
			}
		}
		
		return dataMap;
	}
	
	private Serializable parseColumnValue(Serializable val) {
		if (val == null || "null".equalsIgnoreCase(val.toString())) {
			return "null";
		} else if (val instanceof String) {
			val = DBDataUtil.parseVal(val.toString());
			if(val instanceof String){
				return "'" + val + "'";
			}
		}
		return val;
	}

	public String getTableName() {
		return tableName.trim().split("\\s+")[0];
	}

	public void setTableName(String tableName) {
		if(StringUtils.isNotBlank(tableName)){
			this.tableName = tableName.trim().toLowerCase();
		}
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		if(StringUtils.isNotBlank(keyName)){
			this.keyName = keyName.trim().toLowerCase();
		}
	}

	public String getValueName() {
		return valueName;
	}

	public void setValueName(String valueName) {
		if(StringUtils.isNotBlank(valueName)){
			this.valueName = valueName.trim().toLowerCase();
		}
	}
	
	public boolean isLazyLoad() {
		return lazyLoad;
	}

	public void setLazyLoad(boolean lazyLoad) {
		this.lazyLoad = lazyLoad;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setCondSql(String condSql) {
		this.condSql = condSql;
	}
	
	/*public void setCondSqlGenerator(CondSqlGenerator condSqlGenerator) {
		this.condSqlGenerator = condSqlGenerator;
	}*/

	public ValueConverter getConverter() {
		return converter;
	}

	public void setConverter(ValueConverter converter) {
		this.converter = converter;
	}

	@Override
	public String getRealNs() {
		
		return this.getTableName() + "/" + this.getKeyName() + "/" + this.getValueName();
	}
	
	public static interface ValueConverter{
		
		/**
		 * 属性 valueName 所代表的一行列名和其对应的值的Map。例如，valueName = "name,status", 那么此处的参数将是 
		 *    Map<String,String> valMap = new HashMap<>();
		 *    valMap.put("name", "xxx");
		 *    valMap.put("status", "NORMAL");
		 * @param lineData
		 * @return
		 */
		void convertData(Map<String/*colName*/, Serializable/*colValue*/> lineData);
		
	}

}
