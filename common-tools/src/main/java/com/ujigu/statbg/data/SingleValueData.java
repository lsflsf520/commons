package com.ujigu.statbg.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.util.SpecialCharaterReplaceUtil;

public class SingleValueData implements FlowData {

	private final static Logger LOG = LoggerFactory.getLogger(SingleValueData.class);

	private Object value;
	protected boolean specialCharReplaced;

	public SingleValueData() {

	}

	public SingleValueData(Object value) {

		this.value = value;
	}

	@Override
	public Object getData(String key) {

		Object val = getOriginData(key);
		if (specialCharReplaced) {
			return SpecialCharaterReplaceUtil.replace(val);
		}
		return val;
	}

	protected Object getOriginData(String key) {

		return value;
	}

	@Override
	public void setSpecialCharReplaced(boolean specialCharReplaced) {

		this.specialCharReplaced = specialCharReplaced;
	}

	@Override
	public void appendFlowData(FlowData data) {

		LOG.warn("not supported FlowData type to append" + ",class:" + data.getClass() + ",data:" + data + ",serverIP:"
				+ IPUtil.getLocalIp());
	}

	@Override
	public String toString() {

		return value == null ? null : value.toString();
	}
}
