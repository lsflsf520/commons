package com.xyz.tools.statbg.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.statbg.FlowData;

public class MapData extends SingleValueData {

	private final static Logger LOG = LoggerFactory.getLogger(MapData.class);

	private Map<String, Object> mapData = new LinkedHashMap<String, Object>();

	@Override
	public Object getOriginData(String key) {

		return mapData.get(key);
	}

	public void put(String key, Object val) {

		mapData.put(key, val);
	}

	public Set<Entry<String, Object>> getEntries() {

		return Collections.unmodifiableSet(mapData.entrySet());
	}

	@Override
	public void appendFlowData(FlowData data) {

		if (data == null) {
			return;
		}

		if (data instanceof MapData) {
			MapData targetData = (MapData) data;
			Set<Entry<String, Object>> entries = targetData.getEntries();
			for (Entry<String, Object> entry : entries) {
				mapData.put(entry.getKey(), entry.getValue());
			}
		} else {
			LOG.warn("not supported FlowData type to append" + ",class:" + data.getClass() + ",data:" + data + ",serverIP:"
					+ IPUtil.getLocalIp());
		}
	}

	@Override
	public String toString() {

		return mapData.toString();
	}
}
