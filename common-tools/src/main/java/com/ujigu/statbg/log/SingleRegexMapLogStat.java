package com.ujigu.statbg.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.data.MapData;

public class SingleRegexMapLogStat extends SingleRegexListLogStat {

	private final static Logger LOG = LoggerFactory.getLogger(SingleRegexMapLogStat.class);

	private String[] fieldNames;

	protected String[] getFieldNames() {

		if (fieldNames == null) {
			synchronized (SingleRegexMapLogStat.class) {
				if (fieldNames == null) {
					String currLineRegex = getLineRegex();

					String[] parts = currLineRegex.split("\\([^(]+\\)");

					List<String> fieldNameList = new ArrayList<String>();
					if (parts != null && parts.length > 0) {
						for (int index = 0; index < parts.length; index++) {
							if (parts[index].endsWith("\\(")) {
								String fieldName = parts[index].replace("\\(", "").trim();
								String[] blankChkArr = fieldName.split("\\\\s\\+?"); //去掉空字符串的正则符号，保留后一节（例如^[se]\s+tm，将留下tm）
								if (blankChkArr.length > 1) {
									fieldName = blankChkArr[blankChkArr.length - 1];
								}
								blankChkArr = fieldName.split("\\s+"); //去掉空字符串，保留后一节（例如^[se] tm，将留下tm）
								if (blankChkArr.length > 1) {
									fieldName = blankChkArr[blankChkArr.length - 1];
								}
								fieldNameList.add(fieldName);
							}
						}

						fieldNames = fieldNameList.toArray(new String[0]);
					}
				}
			}
		}

		return fieldNames;
	}

	@Override
	protected FlowData buildValue(List<String> fieldList) {

		String[] fieldNames = getFieldNames();
		MapData mapData = new MapData();
		if (fieldNames == null || fieldNames.length != fieldList.size()) {
			LOG.warn(LOG.isWarnEnabled() ? "fieldNames cannot be null and it's length must be equal to the length of fieldList, fieldNames:"
					+ Arrays.asList(fieldNames) + ",fieldList:" + fieldList + "," + IPUtil.getLocalIp()
					: null);
			return mapData;
		}

		for (int index = 0; index < fieldNames.length; index++) {
			mapData.put(fieldNames[index], fieldList.get(index));
		}

		return mapData;
	}

}
