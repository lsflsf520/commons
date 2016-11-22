package com.yisi.stiku.conf;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.RegexUtil;

/**
 * 默认classpath下的conf/commons.properties, 不需要spring
 * 
 * @author huaiyu.du@opi-corp.com 2012-2-8 下午5:01:28
 */
public final class BaseConfig {

	public enum FileName {
		application
	}

	private static final Map<FileName, Configuration> CONF_MAP = new ConcurrentHashMap<FileName, Configuration>(4);

	private static final Logger LOG = LoggerFactory.getLogger(BaseConfig.class);

	static {
		init();
	}

	private static void init() {

		FileName[] values = FileName.values();
		CONF_MAP.clear();

		for (FileName fileName : values) {
			try {
				String filePath = getPath(fileName + ".properties");
				if (StringUtils.isBlank(filePath)) {
					LOG.warn(LOG.isWarnEnabled() ? "file '" + fileName
							+ ".properties' not exists in classpath, then try to parse '" + fileName + ".xml'" : null);
					filePath = getPath(fileName + ".xml");
					if (StringUtils.isBlank(filePath)) {
						LOG.warn(LOG.isWarnEnabled() ? "No properties file or xml file named '" + fileName
								+ "' exists in classpath or java.user.dir '" + System.getProperty("user.dir") + "'." : null);
						continue;
					}

					LOG.debug(LOG.isDebugEnabled() ? "found file '" + fileName + "'.xml in path '" + filePath + "'" : null);
					CONF_MAP.put(fileName, new XMLConfiguration(filePath));
					continue;
				}

				LOG.debug(LOG.isDebugEnabled() ? "found file '" + fileName + "'.properties in path '" + filePath + "'"
						: null);
				CONF_MAP.put(fileName, new PropertiesConfiguration(filePath));
			} catch (ConfigurationException e) {
				LOG.error(fileName + ".properties file parsed error. msg(" + e.getMessage() + ")", e);
			}
		}
	}

	public static String getPath(String fileName) {

		String filePath = fileName;
		if (new File(filePath).exists()) {
			return fileName;
		}

		String fileSuffix = fileName.startsWith(File.separator) || fileName.startsWith("\\") ? fileName : File.separator
				+ fileName;

		String configDir = System.getProperty("project.config.dir");
		filePath = configDir + fileSuffix;
		if (new File(filePath).exists()) {
			return filePath;
		}

		String userDir = System.getProperty("user.dir");
		filePath = userDir + fileSuffix;
		if (new File(filePath).exists()) {
			return filePath;
		}

		URL url = BaseConfig.class.getResource(fileSuffix);
		if (url == null) {
			url = BaseConfig.class.getClassLoader().getResource(fileName);

			return url == null ? null : url.toString();
		}

		return url.toString();
	}

	/**
	 * 
	 * @param key
	 *            *.properties文件中的key
	 * @return 在CONFIG_FILE_NAME_SET这个集合指定的所有配置文件中,挨个查找key的值,返回第一个找到的值.
	 *         如果一个都没找到，则返回null
	 */
	public static String getValue(String key) {

		return getValue(key, null);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Integer getInt(String key) {

		String val = getValue(key);

		return parseInt(key, val, null);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public static Boolean getBool(String key) {

		String val = getValue(key);

		if ("true".equalsIgnoreCase(val)) {
			return true;
		}

		return false;
	}

	public static String[] getValueArr(String key) {

		FileName[] fileNames = FileName.values();
		for (FileName fileName : fileNames) {
			Configuration config = CONF_MAP.get(fileName);

			if (config == null) {
				// LOG.warn("file with name '" +fileName + "' not found!");
				continue;
			}

			return config.getStringArray(key);
		}

		return null;
	}

	public static int[] getIntArr(String key) {

		String[] valArr = getValueArr(key);

		if (valArr == null || valArr.length <= 0) {
			return new int[0];
		}

		int[] valIntArr = new int[valArr.length];
		for (int index = 0; index < valArr.length; index++) {
			valIntArr[index] = parseInt(key, valArr[index], null);
		}

		return valIntArr;
	}

	private static int parseInt(String key, String val, Integer defaultVal) {

		if (StringUtils.isBlank(val)) {
			return defaultVal;
		}

		if (RegexUtil.isInt(val)) {
			return Integer.valueOf(val);
		}

		throw new BaseRuntimeException("ILLEGAL_DATA", "value '" + val + "' is not integer or not defined for key '" + key
				+ "'");
	}

	/**
	 * 
	 * @param key
	 *            *.properties文件中的key
	 * @param defaultVal
	 *            在*.properties中找不到key所对应的值时，返回该值
	 * @return 在FileName这个枚举类指定的所有配置文件中,挨个查找key的值,返回第一个找到的值.
	 *         如果一个都没找到，则返回defaultVal
	 */
	public static String getValue(String key, String defaultVal) {

		FileName[] fileNames = FileName.values();
		for (FileName fileName : fileNames) {
			Configuration config = CONF_MAP.get(fileName);

			String value = null;
			if (config == null) {
				// LOG.warn("file with name '" +fileName + "' not found!");
				continue;
			}

			String[] values = config.getStringArray(key);
			if (values != null && values.length > 1) {
				value = StringUtils.join(values, ",");
			} else {
				value = config.getString(key);
			}
			if (!StringUtils.isBlank(value)) {
				// LOG.debug("find value for key("+key+") in config file '"+fileName+".properties'");
				return value;
			}
		}

		return defaultVal;
	}

}
