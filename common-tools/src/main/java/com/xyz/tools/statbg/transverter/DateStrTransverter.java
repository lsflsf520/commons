package com.xyz.tools.statbg.transverter;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.utils.DateUtil;
import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.statbg.FlowData;
import com.xyz.tools.statbg.data.SingleValueData;

public class DateStrTransverter extends Transverter {

	private final static Logger LOG = LoggerFactory.getLogger(DateStrTransverter.class);

	protected String pattern;

	@Override
	protected FlowData transvertFlowData(Map<String, List<FlowData>> paramMap,
			FlowData data) {

		if (data == null) {
			LOG.warn("target flow data is null, " + IPUtil.getLocalIp());
			return null;
		}

		Object val = getVal4Key(data);
		if (val == null) {
			LOG.warn("the value of flow data is null for key:" + getTargetKey() + "," + IPUtil.getLocalIp());
			return null;
		}

		Date date = parseDate(val.toString());

		String dateVal = DateUtil.formatDate(date, StringUtils.isBlank(pattern) ? DateUtil.FORMAT_DATE : pattern);

		return new SingleValueData(dateVal);
	}

	protected Date parseDate(String dateStr) {

		return DateUtil.parseDate(dateStr);
	}

	public String getPattern() {

		return pattern;
	}

	public void setPattern(String pattern) {

		this.pattern = pattern;
	}

}
