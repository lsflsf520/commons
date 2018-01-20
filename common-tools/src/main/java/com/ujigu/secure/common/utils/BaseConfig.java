package com.ujigu.secure.common.utils;


import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.exception.BaseRuntimeException;

/**
 * 默认classpath下的conf/commons.properties, 不需要spring
 * 
 * @author lsf 2012-2-8 下午5:01:28
 */
public final class BaseConfig {
	
	private BaseConfig(){
	}

	public enum FileName {
		application
	}

	private static final Map<FileName, Configuration> CONF_MAP = new ConcurrentHashMap<FileName, Configuration>(4);

	private static final Logger LOG = LoggerFactory.getLogger(BaseConfig.class);

	static {
		init();
	}

	public synchronized static void init() {

		FileName[] values = FileName.values();
		//CONF_MAP.clear();

		for (FileName fileName : values) {
			try {
				String filePath = getPath(fileName + ".properties");
				if (StringUtils.isBlank(filePath)) {
					System.out.println("file '" + fileName
							+ ".properties' not exists in classpath, then try to parse '" + fileName + ".xml'");
					filePath = getPath(fileName + ".xml");
					if (StringUtils.isBlank(filePath)) {
						System.out.println("No properties file or xml file named '" + fileName
								+ "' exists in classpath or java.user.dir '" + System.getProperty("user.dir") + "'");
						continue;
					}

					CONF_MAP.put(fileName, new XMLConfiguration(filePath));
					
					System.out.println("found file '" + fileName + "'.xml in path '" + filePath + "'");
					continue;
				}

				CONF_MAP.put(fileName, new PropertiesConfiguration(filePath));
				
				System.out.println("found file '" + fileName + "'.properties in path '" + filePath + "'");
			} catch (ConfigurationException e) {
				System.err.println(fileName + ".properties file parsed error. msg(" + e.getMessage() + ")");
				e.printStackTrace();
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
	public static Integer getInt(String key) {

		String val = getValue(key);

		return parseInt(key, val, null);
	}
	
	/**
	 * 
	 * @param key
	 * @param defVal
	 * @return
	 */
	public static int getInt(String key, int defVal){
		String val = getValue(key);
		
		return parseInt(key, val, defVal);
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
		
		if(defaultVal != null){
			return defaultVal;
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
				for(int index = 0; index < values.length; index++){
					values[index] = replaceVariables(values[index]);
				}
				value = StringUtils.join(values, ",");
			} else {
				value = config.getString(key);
				value = replaceVariables(value);
			}
			if (!StringUtils.isBlank(value)) {
				// LOG.debug("find value for key("+key+") in config file '"+fileName+".properties'");
				return value;
			}
		}

		return defaultVal;
	}
	
	private static String replaceVariables(String value){
		if(StringUtils.isNotBlank(value)){
			List<String> paramNames = RegexUtil.getParamNames(value);
			if(!CollectionUtils.isEmpty(paramNames)){
				for(String paramName : paramNames){
					String paramVal = getValue(paramName);
					if(StringUtils.isNotBlank(paramVal)){
						value = RegexUtil.replaceParamName(value, paramName, paramVal);
					}
				}
			}
		}
		
		return value;
	}
	
	/**
	 * 根据前缀获取键值对信息, 其中的key不包含prefix
	 * @param prefix 
	 * @return 
	 */ 
	public static Map<String, String> getKvMapWithoutPrefix(String prefix){
		Map<String, String> kvMap = new LinkedHashMap<>();
		FileName[] fileNames = FileName.values();
		for (FileName fileName : fileNames) {
			Configuration config = CONF_MAP.get(fileName);

			String value = null;
			if (config == null) {
				// LOG.warn("file with name '" +fileName + "' not found!");
				continue;
			}

			Iterator<String> itr = config.getKeys(prefix);
			while(itr.hasNext()){
				String key = itr.next();
				String[] values = config.getStringArray(key);
				if (values != null && values.length > 1) {
					value = StringUtils.join(values, ",");
				} else {
					value = config.getString(key);
				}
				
				String suffix = key.replace(prefix, "");
				if(suffix.startsWith(".")){
					suffix = suffix.substring(1);
				}
				kvMap.put(suffix, value);
			}
		}

		return kvMap;
	}
	
	/**
	 * 根据前缀获取键值对信息
	 * @param prefix
	 * @return
	 */
	public static Map<String, String> getKvMap(String prefix){
		Map<String, String> kvMap = new LinkedHashMap<>();
		FileName[] fileNames = FileName.values();
		for (FileName fileName : fileNames) {
			Configuration config = CONF_MAP.get(fileName);

			String value = null;
			if (config == null) {
				// LOG.warn("file with name '" +fileName + "' not found!");
				continue;
			}

			Iterator<String> itr = config.getKeys(prefix);
			while(itr.hasNext()){
				String key = itr.next();
				String[] values = config.getStringArray(key);
				if (values != null && values.length > 1) {
					value = StringUtils.join(values, ",");
				} else {
					value = config.getString(key);
				}
				
				kvMap.put(key, value);
			}
		}

		return kvMap;
	}

}
