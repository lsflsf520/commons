package com.yisi.stiku.statbg.log;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.util.RegexHelperUtil;

/**
 * 
 * @author lsf
 *
 */
public class MultiRegexListLogStat extends SingleRegexListLogStat {
	
	private String[] regexArr;
	
	private Pattern[] patternArr;

	/**
	 * 返回的List<String>集合中，第一个元素是匹配到的正则表达式对应的key，即 namePatternMap 中的key
	 */
	@Override
	protected List<String> extractFields(Map<String, List<FlowData>> paramMap,
			String line) {
		if (patternArr == null && regexArr != null) {
			synchronized (this) {
				if (patternArr == null) {
					patternArr = new Pattern[regexArr.length];
					for(int i=0; i<regexArr.length; i++){
						patternArr[i] = Pattern.compile(regexArr[i]);
					}
				}
			}
		}
		
		if(patternArr != null){
			for(Pattern pattern : patternArr){
				List<String> values = RegexHelperUtil.extractGroups(pattern, line);
				if(values != null && values.size() > 0){
					return values;
				}
			}
		}

		return null;
	}

	public String[] getRegexArr() {
		return regexArr;
	}

	public void setRegexArr(String[] regexArr) {
		this.regexArr = regexArr;
	}

}
