package com.xyz.tools.statbg.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.xyz.tools.statbg.FlowData;
import com.xyz.tools.statbg.util.RegexHelperUtil;

/**
 * 
 * @author lsf
 *
 */
public class MultiNameRegexListLogStat extends SingleRegexListLogStat {
	
	private Map<String, String> nameRegexMap ;
	
	private Map<String,Pattern> namePatternMap;

	/**
	 * 返回的List<String>集合中，第一个元素是匹配到的正则表达式对应的key，即 namePatternMap 中的key
	 */
	@Override
	protected List<String> extractFields(Map<String, List<FlowData>> paramMap,
			String line) {
		if (namePatternMap == null && nameRegexMap != null) {
			synchronized (this) {
				if (namePatternMap == null) {
					namePatternMap = new HashMap<String, Pattern>();
					Set<Entry<String, String>> entries = nameRegexMap.entrySet();
				    for(Entry<String, String> entry: entries){
				    	namePatternMap.put(entry.getKey(), Pattern.compile(entry.getValue()));
				    }
				}
			}
		}
		
		if(namePatternMap != null){
			Set<Entry<String, Pattern>> entries = namePatternMap.entrySet();
		
			for(Entry<String, Pattern> entry : entries){
				List<String> values = RegexHelperUtil.extractGroups(entry.getValue(), line);
				if(values != null && values.size() > 0){
					values.add(0, entry.getKey());
					return values;
				}
			}
		}

		return null;
	}

//	protected FlowData buildValue(Map<String,List<String>> valueMap) {
//		MapMultiValueData mapData = new MapMultiValueData();
//		if (valueMap != null && valueMap.size() > 0) {
//			Set<Entry<String, List<String>>> entries = valueMap.entrySet();
//			for (Entry<String, List<String>> entry : entries) {
//				List<Serializable> values = new ArrayList<Serializable>();
//				
//				List<String> valueList = entry.getValue();
//				if(valueList != null ){
//					for(String val : valueList){
//						values.add(val);
//					}
//				}
//				
//				mapData.put(entry.getKey(), values);
//			}
//		}
//		return mapData;
//	}

	public Map<String, String> getNameRegexMap() {
		return nameRegexMap;
	}

	public void setNameRegexMap(Map<String, String> nameRegexMap) {
		this.nameRegexMap = nameRegexMap;
	}

}
