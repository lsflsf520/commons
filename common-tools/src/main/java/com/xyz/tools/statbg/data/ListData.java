package com.xyz.tools.statbg.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.common.utils.RegexUtil;
import com.xyz.tools.statbg.FlowData;

public class ListData extends SingleValueData {

	private final static Logger LOG = LoggerFactory.getLogger(ListData.class);

	private List<Object> listData = new ArrayList<Object>();

	@Override
	public Object getOriginData(String key) {

		if (RegexUtil.isInt(key)) {
			int index = Integer.valueOf(key);
			if (index >= 0 && index < listData.size()) {
				return listData.get(index);
			}
		}

		return null;
	}

	public void add(Serializable val) {

		listData.add(val);
	}

	public Iterator<Object> getIterator() {

		return listData.iterator();
	}

	@Override
	public void appendFlowData(FlowData data) {

		if (data == null) {
			return;
		}
		if (data instanceof SingleValueData) {
			listData.add(data.getData(null));
		} else if (data instanceof ListData) {
			ListData targetData = (ListData) data;
			Iterator<Object> itr = targetData.getIterator();
			while (itr.hasNext()) {
				listData.add(itr.next());
			}
		} else if (data instanceof MapData) {
			MapData targetData = (MapData) data;
			Set<Entry<String, Object>> entries = targetData.getEntries();
			for (Entry<String, Object> entry : entries) {
				listData.add(entry.getValue());
			}
		} else {
			LOG.warn("not supported FlowData type to append" + ",class:" + data.getClass() + ",data:" + data + ",serverIP:"
					+ IPUtil.getLocalIp());
		}
	}

	@Override
	public String toString() {

		return listData.toString();
	}

}
