package com.yisi.stiku.onoff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.RegexUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;

/**
 * @author shangfeng
 *
 */
public class SwitchTool {

	private final static String SWITCH_BASE_PATH = ZkConstant.ZK_ROOT_NODE + "/switch/";
	private final static String ON = "on";
	private final static String OFF = "off";

	public static void setTimeout(String namespace, Serializable id, Date date) {

		if (date == null || date.getTime() <= System.currentTimeMillis()) {
			throw new BaseRuntimeException("ILLEGAL_PARAM",
					"param date cannot be null, and also cannot be less than current time.");
		}
		String path = buildSwitchPath(namespace, id);
		if (!ConfigOnZk.exists(path)) {
			ConfigOnZk.createPersistNode(path);
		}

		ConfigOnZk.setData(path, date.getTime() + "");
	}

	public static void setTimeout(String namespace, Map<String, Serializable> paramMap, Date date) {

		String id = join2Str(paramMap);

		setTimeout(namespace, id, date);
	}

	public static void setTimeout(String namespace, Date date) {

		setTimeout(namespace, "", date);
	}

	public static void setOn(String namespace) {

		setOn(namespace, "");
	}

	public static void setOff(String namespace) {

		setOff(namespace, "");
	}

	public static void setOn(String namespace, Serializable id) {

		String path = buildSwitchPath(namespace, id);
		if (!ConfigOnZk.exists(path)) {
			ConfigOnZk.createPersistNode(path);
		}

		ConfigOnZk.setData(path, ON);
	}

	public static void setOn(String namespace, Map<String, Serializable> paramMap) {

		String id = join2Str(paramMap);
		setOn(namespace, id);
	}

	public static void setOff(String namespace, Map<String, Serializable> paramMap) {

		String id = join2Str(paramMap);
		setOff(namespace, id);
	}

	public static void setOff(String namespace, Serializable id) {

		String path = buildSwitchPath(namespace, id);
		if (ConfigOnZk.exists(path)) {
			ConfigOnZk.setData(path, OFF);
		}
	}

	public static boolean isOn(String namespace) {

		return isOn(namespace, "");
	}

	public static boolean isOff(String namespace) {

		return isOff(namespace, "");
	}

	public static boolean isOn(String namespace, Serializable id) {

		String path = buildSwitchPath(namespace, id);

		String value = null;
		return ConfigOnZk.exists(path)
				&& (ON.equalsIgnoreCase(value = ConfigOnZk.getData(path)) || (RegexUtil.isInt(value) && Long.valueOf(value) > new Date()
						.getTime()));
	}

	public static boolean isOff(String namespace, Serializable id) {

		return !isOn(namespace, id);
	}

	public static boolean isOn(String namespace, Map<String, Serializable> paramMap) {

		String id = join2Str(paramMap);
		return isOn(namespace, id);
	}

	public static boolean isOff(String namespace, Map<String, Serializable> paramMap) {

		return !isOn(namespace, paramMap);
	}

	private static String join2Str(Map<String, Serializable> paramMap) {

		if (paramMap == null || paramMap.isEmpty()) {
			return null;
		}
		List<String> keys = new ArrayList<String>();
		for (String key : paramMap.keySet()) {
			keys.add(key);
		}

		Collections.sort(keys);

		String id = keys.get(0) + ":" + paramMap.get(keys.get(0));
		for (int index = 1; index < keys.size(); index++) {
			id += "_" + keys.get(index) + ":" + paramMap.get(keys.get(index));
		}

		return id;
	}

	private static String buildSwitchPath(String namespace, Serializable id) {

		return SWITCH_BASE_PATH + namespace + (id != null && StringUtils.isNotBlank(id.toString()) ? "/" + id : "");
	}

}
