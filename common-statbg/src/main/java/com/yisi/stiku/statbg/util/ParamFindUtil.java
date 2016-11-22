package com.yisi.stiku.statbg.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.exception.NotSupportException;

/**
 * 
 * @author lsf
 *
 */
public class ParamFindUtil {

	private final static Logger LOG = LoggerFactory.getLogger(ParamFindUtil.class);

	/**
	 * 
	 * @param paramName
	 * @param objMap
	 * @return
	 */
	public static Object getValue(String paramName, Map<String, List<FlowData>> paramMap, int lineIndex,
			boolean repeatOneline) {

		String[] parts = paramName.split("\\.");
		if (parts.length > 2) {
			throw new NotSupportException("param with only one point(like 'person.name') supported so far. param("
					+ paramName + ") is not supported.");
		}
		List<FlowData> listData = paramMap.get(parts[0]);
		if (listData == null || listData.size() <= 0) {
			// throw new ParamNotExistException("param '" + parts[0]
			// + "' does not exist for paramName '"+paramName+"'.");
			LOG.debug(LOG.isDebugEnabled() ? "param not exists, param '" + parts[0] + "', paramName '" + paramName + "'"
					+ IPUtil.getLocalIp() : null);
			return null;
		}

		if (parts.length <= 1 || lineIndex < 0) {
			return listData.get(0).getData(null);
		}

		if (repeatOneline && listData.size() == 1) {
			return listData.get(0).getData(parts[1]);
		}

		if (lineIndex >= listData.size()) {
			// throw new
			// ParamNotExistException("lineIndex '"+lineIndex+"' out of bounds in listData with paramName '"+parts[0]+"'.");
			LOG.debug(LOG.isDebugEnabled() ? "index out of bounds, lineIndex '" + lineIndex + "', paramName '" + paramName
					+ "'" +
					IPUtil.getLocalIp() : null);
			return null;
		}

		FlowData fieldData = listData.get(lineIndex); // 根据行号获取对应的行数据

		return fieldData.getData(parts[1]);
	}

}
