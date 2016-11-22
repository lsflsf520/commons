package com.yisi.stiku.statbg.transverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.Stat;

/**
 * 数据转换器的父类
 * 
 * @author lsf
 *
 */
abstract public class Transverter extends Stat {

	private final static Logger LOG = LoggerFactory.getLogger(Transverter.class);

	private String targetOutputKey; // paramMap 的key， 即Stat的outputKey,
									// 不是实际目标数据的key
	private String targetDataKey; // 能从targetOutputKey所对应的FlowData中取到数据的key

	private String targetKey;

	protected boolean specialCharReplaced;
	protected boolean append2Line = false;

	@Override
	public List<FlowData> execute(Map<String, List<FlowData>> paramMap) {

		List<FlowData> targetList = paramMap.get(targetOutputKey);

		if (targetList == null) {
			LOG.warn("target Object is null, targetKey:" + getTargetKey() + "," + IPUtil.getLocalIp());
			return targetList;
		}

		List<FlowData> resultObj = new ArrayList<FlowData>();

		for (FlowData data : targetList) {
			FlowData resultData = transvertFlowData(paramMap, data);
			if (resultData != null) {
				resultObj.add(resultData);

				if (isAppend2Line()) {
					data.appendFlowData(resultData);
				}
			}
		}

		return resultObj;
	}

	/**
	 * 
	 * @param paramMap
	 *            上下文参数对象
	 * @param data
	 *            需要被处理的数据对象
	 * @return 返回处理完后的数据对象
	 */
	abstract protected FlowData transvertFlowData(Map<String, List<FlowData>> paramMap, FlowData data);

	protected Object getVal4Key(FlowData flowData) {

		return flowData == null ? null : flowData.getData(targetDataKey);
	}

	public void setTargetKey(String targetKey) {

		if (StringUtils.isBlank(targetKey)) {
			throw new BaseRuntimeException("PARAM_ERROR", "targetKey must be defined in Transverter");
		}

		String[] parts = targetKey.split("\\.");
		this.targetOutputKey = parts[0];
		if (parts.length == 2) {
			this.targetDataKey = parts[1];
		}

		this.targetKey = targetKey;
	}

	public String getTargetKey() {

		return targetKey;
	}

	@Override
	public boolean isSpecialCharReplaced() {

		return specialCharReplaced;
	}

	public void setSpecialCharReplaced(boolean specialCharReplaced) {

		this.specialCharReplaced = specialCharReplaced;
	}

	public boolean isAppend2Line() {

		return append2Line;
	}

	public void setAppend2Line(boolean append2Line) {

		this.append2Line = append2Line;
	}
}
