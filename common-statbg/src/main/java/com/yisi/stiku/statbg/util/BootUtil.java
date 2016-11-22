package com.yisi.stiku.statbg.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.conf.BaseConfig;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.GlobalParam;
import com.yisi.stiku.statbg.Stat;
import com.yisi.stiku.statbg.data.SingleValueData;

/**
 * @author shangfeng
 *
 */
public class BootUtil {

	private final static Logger LOG = LoggerFactory.getLogger(BootUtil.class);

	public static void execStat(String[] args) {

		long startTime = System.currentTimeMillis();
		String springFile = getSpringFile(args);

		LOG.debug("use config file '" + springFile + "'");
		// ApplicationContext context = new ClassPathXmlApplicationContext(
		// springFile);
		ApplicationContext context = new FileSystemXmlApplicationContext(springFile);

		Map<String, List<FlowData>> paramMap = getGlobalParams(context, args);

		if (LOG.isDebugEnabled()) {
			LOG.debug("global params:" + joinGlobalParams(paramMap));
		}

		List<Stat> stats = getStatList(context);
		for (Stat stat : stats) {
			LOG.debug("stat:" + stat + ",orderNO:" + stat.getOrderNO());
			List<FlowData> outputList = null;

			try {
				outputList = stat.execute(paramMap);
			} catch (Exception ex) {
				LOG.error("exec for stat(" + stat + ") with paramMap("
						+ paramMap + ") failure. serverIP:" + IPUtil.getLocalIp(), ex);
				if (stat.isExitWhenException()) {
					break;
				}
			}

			if (outputList == null || outputList.size() <= 0 ||
					StringUtils.isBlank(stat.getOutputKey())) {
				continue;
			}

			for (FlowData flowData : outputList) {
				flowData.setSpecialCharReplaced(stat.isSpecialCharReplaced());
			}

			paramMap.put(stat.getOutputKey(), outputList);
		}

		LOG.debug("task has exec over in " + (System.currentTimeMillis() - startTime) + " miliseconds.");
	}

	private static String joinGlobalParams(Map<String, List<FlowData>> paramMap) {

		StringBuilder builder = new StringBuilder();
		String fieldSpliter = ", ";
		if (paramMap != null) {
			Set<String> keyset = paramMap.keySet();
			for (String key : keyset) {
				List<FlowData> dataList = paramMap.get(key);
				builder.append(key);
				builder.append("=");
				if (dataList != null && dataList.size() == 1) {
					builder.append(dataList.get(0).getData(key));
				} else {
					builder.append(dataList);
				}
				builder.append(fieldSpliter);
			}

			if (builder.length() > 0) {
				builder.setLength(builder.length() - fieldSpliter.length());
			}
		}

		return builder.toString();
	}

	private static List<Stat> getStatList(ApplicationContext context) {

		String[] statNames = context.getBeanNamesForType(Stat.class);

		if (statNames == null || statNames.length <= 0) {
			LOG.warn("the number of statNames can not be less than 0, statNames: " + statNames);

			System.exit(1);
		}

		List<Stat> statList = new ArrayList<Stat>();
		for (String statName : statNames) {
			Stat stat = (Stat) context.getBean(statName);

			statList.add(stat);

		}

		Collections.sort(statList, new Comparator<Stat>() {

			@Override
			public int compare(Stat arg0, Stat arg1) {

				return new Integer(arg0.getOrderNO()).compareTo(new Integer(
						arg1.getOrderNO()));
			}

		});

		return statList;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, List<FlowData>> getGlobalParams(
			ApplicationContext context, String[] args) {

		Map<String, List<FlowData>> globalParamMap = new HashMap<String, List<FlowData>>();

		String[] globalParamNames = context
				.getBeanNamesForType(GlobalParam.class);
		if (globalParamNames != null && globalParamNames.length > 0) {
			for (String globalParam : globalParamNames) {
				GlobalParam<Serializable> param = (GlobalParam<Serializable>) context
						.getBean(globalParam);
				List<FlowData> singleDataList = new ArrayList<FlowData>();
				singleDataList.add(new SingleValueData(param.generateParam()));

				globalParamMap.put(globalParam, singleDataList);
			}
		}

		// 定义好命令行参数，如果命令行参数的名称和globalParamNames中有相同的名称，则优先使用命令行对应的参数
		for (int index = 0; index < args.length; index = index + 2) {
			if (index + 1 < args.length) {
				String argName = args[index];
				String argVal = args[index + 1];

				if (argName.startsWith("-")) {
					argName = argName.substring(1);
				}

				List<FlowData> singleDataList = new ArrayList<FlowData>();
				singleDataList.add(new SingleValueData(argVal));
				globalParamMap.put(argName, singleDataList);
			}
		}

		return globalParamMap;
	}

	private static String getSpringFile(String[] args) {

		String springFile = "context/spring-db-stat.xml";
		if (args != null && args.length > 0) {
			for (int index = 0; index < args.length; index = index + 2) {
				if (index + 1 < args.length && "-configFile".equals(args[index])) {
					springFile = args[index + 1];
					break;
				}
			}
		}

		return BaseConfig.getPath(springFile);
	}
}
