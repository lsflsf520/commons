package com.ujigu.statbg.transverter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.data.MapData;

/**
 * 使用此类，会将多个结果集，根据targetKeyMap中指定的join key组合成一个结果集。
 * 例如：
 *    第一个结果集： 
 *      userId  userName userAge
 *       123      刘亦菲        26
 *       124      范爷           28
 *    第二个结果集：
 *      userId  addr   company
 *       123    上海          广电传媒
 *       124    北京          北电传媒
 *   如果这个两个结果集都以 userId 为key，那么将组合成如下结果集：
 *      userId  userName userAge   addr   company
 *       123      刘亦菲        26      上海          广电传媒
 *       124      范爷           28      北京          北电传媒
 * 
 * @author shangfeng
 *
 */
public class MapDataJoinTransverter extends Transverter {

	private final static Logger LOG = LoggerFactory.getLogger(MapDataJoinTransverter.class);

	private Map<String /* output key */, String /* join key */> targetKeyMap;
	private Map<String/* [output key.]join key */, String /* alias name */> aliasKeyMap;
	private boolean excludeJoinKey = true;

	@Override
	public List<FlowData> execute(Map<String, List<FlowData>> paramMap) {

		if (targetKeyMap == null || targetKeyMap.size() < 2) {
			LOG.warn("the size of property targetKeys in com.yisi.stiku.statbg.transverter.DataJoinTransverter should be more than one.");
			return null;
		}

		Map<Object, MapData> resultDataMap = new LinkedHashMap<Object, MapData>();
		for (String outputkey : targetKeyMap.keySet()) {
			List<FlowData> targetList = paramMap.get(outputkey);

			if (targetList == null || targetList.isEmpty()) {
				LOG.warn("target Object is null or empty, targetKey:" + outputkey + "," + IPUtil.getLocalIp());
				continue;
			}

			for (FlowData flowData : targetList) {
				if (!(flowData instanceof MapData)) {
					LOG.warn("data:" + flowData + " is not instanceof MapData, and it will be ignored.");
					continue;
				}

				MapData mapData = (MapData) flowData;
				String originJoinKey = targetKeyMap.get(outputkey);
//				Object joinVal = mapData.getData(originJoinKey);
				Object joinVal = buildJoinKey(mapData, originJoinKey);

				MapData existMapData = resultDataMap.get(joinVal);
				if (existMapData == null) {
					existMapData = new MapData();
					String aliasKey = getAliasKeyName(outputkey, originJoinKey);
					existMapData.put(aliasKey, joinVal);

					resultDataMap.put(joinVal, existMapData);
				}
				for (Entry<String, Object> entry : mapData.getEntries()) {
					if (this.excludeJoinKey && StringUtils.isNotBlank(entry.getKey())
							&& entry.getKey().equals(originJoinKey)) {
						continue;
					}

					String aliasKey = getAliasKeyName(outputkey, entry.getKey());
					existMapData.put(aliasKey, entry.getValue());
				}
			}
		}

		List<FlowData> flowDataList = new ArrayList<FlowData>();
		for (MapData mapData : resultDataMap.values()) {
			flowDataList.add(mapData);
		}

		return flowDataList;
	}
	
	private Object buildJoinKey(MapData mapData, String originJoinKey){
		String[] fields = originJoinKey.split(",");
		if(fields.length <= 1){
			return mapData.getData(originJoinKey);
		}
		
		String keyval = "";
		for(String field : fields){
			keyval += mapData.getData(field.trim());
		}
		
		return keyval;
	}

	private String getAliasKeyName(String outputKey, String originKey) {

		if (this.aliasKeyMap != null) {
			if (this.aliasKeyMap.containsKey(outputKey + "." + originKey)) {
				return this.aliasKeyMap.get(outputKey + "." + originKey);
			}

			if (this.aliasKeyMap.containsKey(originKey)) {
				return this.aliasKeyMap.get(originKey);
			}
		}

		return originKey;
	}

	@Override
	protected FlowData transvertFlowData(Map<String, List<FlowData>> paramMap, FlowData data) {

		// Do nothing

		return null;
	}

	public void setTargetKeyMap(Map<String, String> targetKeyMap) {

		this.targetKeyMap = targetKeyMap;
	}

	public void setAliasKeyMap(Map<String, String> aliasKeyMap) {

		this.aliasKeyMap = aliasKeyMap;
	}

	public void setExcludeJoinKey(boolean excludeJoinKey) {

		this.excludeJoinKey = excludeJoinKey;
	}

}
