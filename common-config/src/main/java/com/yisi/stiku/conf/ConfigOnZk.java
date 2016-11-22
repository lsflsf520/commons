package com.yisi.stiku.conf;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.RegexUtil;

/**
 * <p>
 * 本类为单例，一旦初始化就没法改变，如果你需要指定自己的配置路径，在使用本类之前先调用BaseConfig.setConfigFrom()
 * </p>
 * <p>
 * 异常code 类似HTTP，4XX系列是业务导致的，例如路径、格式不对；5XX是系统的问题，例如网络问题
 * </p>
 * <p>
 * watch的功能是带监控的，一旦改变节点的值，就能捕捉到。使用的时候还是需要知道配置路径
 * <p>
 * 本类的方法<b><i> 只能</i> </b>watch .xml或者.properties文件，如果不想用本类提供的接口， 可以自己写：
 * </p>
 * ZKClient.get().watchStrValueNode( watcher ) watcher需要自己实现一个 new
 * StringValueWatcher() 内部类
 * <p>
 * 如果配置参数是永远不变，也可以不用watch直接获取节点的字符串内容:
 * </p>
 * ZKClient.get().getStringData( node )
 * 
 * @author guoning.liang@opi-corp.com
 *
 */
public class ConfigOnZk {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigOnZk.class);

	private static String zkHostPattern = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}(:\\d{1,5}){0,1},)*((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}(:\\d{1,5}){0,1})$";

	public final static String ZKCONFIGS_CHROOT = BaseConfig.getValue(
			"zk.config.prefix", ZkConstant.ZK_ROOT_NODE);

	private static Map<String, Configuration> totalConfigs = new ConcurrentHashMap<String, Configuration>();

	private final static ZkClient zkClient;

	private ConfigOnZk() {

	}

	static {
		if (!BaseConfig.getBool("zk.not.used")) {
			String zkQuorum = getZkConnStr();

			List<String> zkHostList = new ArrayList<String>();

			String[] connectAddress = null;
			if (Pattern.matches(zkHostPattern, zkQuorum)) {
				LOG.debug("use zk.nodes hosts.....:" + zkQuorum);
				connectAddress = zkQuorum.split(",");
			} else {
				throw new BaseRuntimeException("NOT_EXIST",
						"zk.nodes must be defined in application.properties");
			}
			Collections.addAll(zkHostList, connectAddress);
			Collections.shuffle(zkHostList);
			// zooKeeper = new ZooKeeper(StringUtils.join(zkHostList, ","), new
			// Integer(BaseConfig.getValue("zk.session.timeout", "30000")),
			// defaultWatch);
			zkClient = new ZkClient(StringUtils.join(zkHostList, ","), 2000);
		} else {
			LOG.warn("zk has not init yet. because property 'zk.not.used' has been set true");
			zkClient = null;
		}
	}

	/**
	 * 
	 * @return 返回用于可以操作zk的ZkClient对象
	 */
	// public static ZkClient getZkClient() {
	// return zkClient;
	// }

	public static String getZkConnStr() {

		return BaseConfig.getValue("zk.nodes", "");
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static List<String> getChildren(String path) {

		return zkClient.getChildren(path);
	}

	public static byte[] getByteData(String path) {

		return zkClient.readData(path, true);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String getData(String path) {

		byte[] datas = getByteData(path);

		try {
			return datas == null ? null : new String(datas, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.warn("get data from " + path + " error, errorMsg:"
					+ e.getMessage());
		}

		return null;
	}

	/**
	 * 
	 * @param path
	 * @param data
	 */
	public static void setData(String path, String data) {

		data = StringUtils.isBlank(data) ? "" : data.trim();
		try {
			zkClient.writeData(path, data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new BaseRuntimeException("ENCODE_ERROR",
					"系统异常", "path:" + path + ",data:" + data, e);
		}
	}

	/**
	 * 
	 * @param path
	 *            删除节点及其所有子节点
	 */
	public static void delNode(String path) {

		zkClient.deleteRecursive(path);
	}

	/**
	 * 
	 * @param path
	 */
	public static void createPersistNode(String path) {

		zkClient.createPersistent(path, true);
	}

	/**
	 * 
	 * @param path
	 */
	public static void createEphemeralNode(String path) {

		ensureCanCreate(path);

		zkClient.createEphemeral(path);
	}

	public static void createEphemeralNode(String path, String content) {

		ensureCanCreate(path);

		zkClient.createEphemeral(path, content.getBytes());
	}

	public static String createEphemeralSequential(String path) {

		return createEphemeralSequential(path, "");
	}

	public static String createEphemeralSequential(String path, String content) {

		ensureCanCreate(path);

		return zkClient.createEphemeralSequential(path, content.getBytes());
	}

	/**
	 * 
	 * @param path
	 *            确保父节点已经存在,并且path节点不存在（如果存在则将被删除）
	 */
	private static void ensureCanCreate(String path) {

		String parentPath = path.substring(0, path.lastIndexOf("/"));
		if (!exists(parentPath)) {
			createPersistNode(parentPath);
		} else if (exists(path)) {
			delNode(path);
		}
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static boolean exists(String path) {

		return zkClient.exists(path);
	}

	/**
	 * 
	 * @param parentPath
	 * @param childListener
	 * @return
	 */
	public static List<String> subscribeChildChanges(String parentPath, IZkChildListener childListener) {

		return zkClient.subscribeChildChanges(parentPath, childListener);
	}

	public static void unsubscribeChildChanges(String parentPath, IZkChildListener childListener) {

		zkClient.unsubscribeChildChanges(parentPath, childListener);
	}

	/**
	 * 
	 * @param nodePath
	 * @param dataListener
	 */
	public static void subscribeDataChanges(String nodePath, IZkDataListener dataListener) {

		zkClient.subscribeDataChanges(nodePath, dataListener);
	}

	public static void unsubscribeDataChanges(String nodePath, IZkDataListener dataListener) {

		zkClient.unsubscribeDataChanges(nodePath, dataListener);
	}

	/**
	 * 
	 * @param stateListener
	 */
	public static void subscribeStateChanges(IZkStateListener stateListener) {

		zkClient.subscribeStateChanges(stateListener);
	}

	public static void unsubscribeStateChanges(IZkStateListener stateListener) {

		zkClient.unsubscribeStateChanges(stateListener);
	}

	/**
	 * 
	 * @param pathFromMlwconf
	 *            从ZkConstant.ZK_ROOT_NODE下开始，也就是不带ZkConstant.ZK_ROOT_NODE，必须是.
	 *            xml或者.properties结尾
	 * @param content
	 *            pathFromMlwconf节点中的内容
	 * @param configType
	 *            配置文件后缀
	 */
	private static void loadConfig(final String pathFromZk, String content,
			String configType) {

		if (configType.equals(".xml")) {
			Configuration conf = new XMLConfiguration();
			try {
				((XMLConfiguration) conf).load(
						new ByteArrayInputStream(content.getBytes()), "UTF-8");
			} catch (ConfigurationException e) {
				LOG.error("parse config : " + pathFromZk + " exception!", e);
			}
			totalConfigs.put(pathFromZk, conf);

			zkClient.subscribeDataChanges(pathFromZk, new IZkDataListener() {

				@Override
				public void handleDataDeleted(String dataPath) throws Exception {

					totalConfigs.remove(dataPath);
				}

				@Override
				public void handleDataChange(String dataPath, byte[] data)
						throws Exception {

					Configuration conf = new XMLConfiguration();
					try {
						((XMLConfiguration) conf).load(
								new ByteArrayInputStream(data), "UTF-8");
					} catch (ConfigurationException e) {
						LOG.error("parse config : " + pathFromZk
								+ " exception!", e);
					}
					totalConfigs.put(pathFromZk, conf);
				}
			});

		} else {
			Configuration conf = new PropertiesConfiguration();
			try {
				((PropertiesConfiguration) conf).load(new ByteArrayInputStream(
						content.getBytes()), "UTF-8");
			} catch (ConfigurationException e) {
				LOG.error("parse config : " + pathFromZk + " exception!", e);
			}
			totalConfigs.put(pathFromZk, conf);

			zkClient.subscribeDataChanges(pathFromZk, new IZkDataListener() {

				@Override
				public void handleDataDeleted(String dataPath) throws Exception {

					totalConfigs.remove(dataPath);
				}

				@Override
				public void handleDataChange(String dataPath, byte[] data)
						throws Exception {

					Configuration conf = new PropertiesConfiguration();
					try {
						((PropertiesConfiguration) conf).load(
								new ByteArrayInputStream(data), "UTF-8");
					} catch (ConfigurationException e) {
						LOG.error("parse config : " + pathFromZk
								+ " exception!", e);
					}
					totalConfigs.put(pathFromZk, conf);
				}

			});

		}

	}

	/**
	 * 注册监控一个节点，并拿回节点的配置信息
	 * 
	 * @param pathFromZk
	 *            , 从ZkConstant.ZK_ROOT_NODE下开始，也就是不带ZkConstant.ZK_ROOT_NODE，必须是
	 *            .xml或者.properties结尾
	 * @return
	 * @throws BaseRuntimeException
	 *             出现路径错误，格式错误；或者网络链接错误
	 */
	private static Configuration getConfigAndWatch(String pathFromZk) {

		if (pathFromZk.startsWith("/+")) {
			pathFromZk = pathFromZk.replaceFirst("/+", "/");
		}

		if (!pathFromZk.startsWith("/")) {
			pathFromZk = ZKCONFIGS_CHROOT + "/" + pathFromZk;
		}

		if (!exists(pathFromZk)) {
			return null;
		}

		String content = getData(pathFromZk);
		if (StringUtils.isNotBlank(content)) {
			int lastDotIndex = pathFromZk.lastIndexOf(".");
			final String configType = pathFromZk.substring(lastDotIndex);
			if (totalConfigs.containsKey(pathFromZk)) {
				return totalConfigs.get(pathFromZk);
			} else {
				loadConfig(pathFromZk, content, configType);
				return totalConfigs.get(pathFromZk);
			}
		}

		return null;
	}

	/**
	 * 注册监控一个节点，并拿回节点的配置信息, 直接取配置里的Key value
	 * 
	 * @param pathFromZk
	 *            , 从ZkConstant.ZK_ROOT_NODE下开始，也就是不带ZkConstant.ZK_ROOT_NODE，必须是
	 *            .xml或者.properties结尾
	 * @param key
	 *            property文件中某键值对
	 * @return
	 * @throws BaseRuntimeException
	 */
	public static String getValue(String pathFromZk, String key) {

		Configuration conf = getConfigAndWatch(pathFromZk);
		if (conf == null) {
			return null;
		}
		String value = null;
		String[] values = conf.getStringArray(key);
		if (values != null && values.length > 1) {
			value = StringUtils.join(values, ",");
		} else {
			value = conf.getString(key);
		}
		return value;
	}

	public static Integer getInt(String pathFromZk, String key, Integer defaultVal) {

		String val = getValue(pathFromZk, key);

		return parseInt(pathFromZk, key, val, defaultVal);
	}

	public static Integer getInt(String pathFromZk, String key) {

		return getInt(pathFromZk, key, null);
	}

	public static Boolean getBool(String pathFromZk, String key) {

		String val = getValue(pathFromZk, key);

		if ("true".equalsIgnoreCase(val)) {
			return true;
		}

		return false;
	}

	private static Integer parseInt(String pathFromZk, String key, String val, Integer defaultVal) {

		if (StringUtils.isBlank(val)) {
			return defaultVal;
		}

		if (RegexUtil.isInt(val)) {
			return Integer.valueOf(val);
		}

		throw new BaseRuntimeException("ILLEGAL_DATA", "value '" + val + "' is not integer or not defined for key '" + key
				+ "' in node '"
				+ pathFromZk + "'");
	}

	public static int[] getIntArr(String pathFromZk, String key) {

		String[] valArr = getValueArr(pathFromZk, key);

		if (valArr == null || valArr.length <= 0) {
			return new int[0];
		}

		int[] valIntArr = new int[valArr.length];
		for (int index = 0; index < valArr.length; index++) {
			valIntArr[index] = parseInt(pathFromZk, key, valArr[index], null);
		}

		return valIntArr;
	}

	/**
	 * 
	 * @param pathFromZk
	 * @return 返回zk节点pathFromZk中的属性key列表
	 */
	public static Iterator<String> getKeys(String pathFromZk) {

		Configuration conf = getConfigAndWatch(pathFromZk);
		if (conf == null) {
			return null;
		}
		return conf.getKeys();
	}

	/**
	 * 
	 * @param pathFromZk
	 * @param prefix
	 * @return 返回zk节点pathFromZk中的以prefix开头的属性key列表
	 */
	public static Iterator<String> getKeys(String pathFromZk, String prefix) {

		Configuration conf = getConfigAndWatch(pathFromZk);
		if (conf == null) {
			return null;
		}
		return conf.getKeys(prefix);
	}

	/**
	 * 获取pathFromZk节点中配置名为key的值，以字符数组的形式返回
	 * 
	 * @param pathFromZk
	 * @param key
	 * @return
	 */
	public static String[] getValueArr(String pathFromZk, String key) {

		Configuration conf = getConfigAndWatch(pathFromZk);
		if (conf == null) {
			return null;
		}

		return conf.getStringArray(key);
	}

	/**
	 * 注册监控一个节点，并拿回节点的配置信息, 直接取配置里的Key value 默认值
	 * 
	 * @param pathFromZk
	 *            , 从ZkConstant.ZK_ROOT_NODE下开始，也就是不带ZkConstant.ZK_ROOT_NODE，必须是
	 *            .xml或者.properties结尾
	 * @param key
	 * @return
	 * @throws BaseRuntimeException
	 */
	public static String getValue(String pathFromZk, String key,
			String defaultValue) {

		String value = getValue(pathFromZk, key);

		return StringUtils.isBlank(value) ? defaultValue : value;
	}

	/**
	 * 注册监控一个节点，并拿回节点的配置信息, 直接取配置里的Key value 默认值
	 * 
	 * @param pathFromZk
	 *            , 从ZkConstant.ZK_ROOT_NODE下开始，也就是不带ZkConstant.ZK_ROOT_NODE，必须是
	 *            .xml或者.properties结尾
	 * @param key
	 * @return
	 * @throws BaseRuntimeException
	 */
	@SuppressWarnings("rawtypes")
	public static int getElemSize(String pathFromZk, String selector) {

		Configuration config = getConfigAndWatch(pathFromZk);
		if (config == null) {
			return 0;
		}
		Object element = config.getProperty(selector);
		if (element == null) {
			return 0;
		}

		if (element instanceof Collection) {
			Collection collec = (Collection) config.getProperty(selector);
			return collec.size();
		}

		return 1;
	}

}
