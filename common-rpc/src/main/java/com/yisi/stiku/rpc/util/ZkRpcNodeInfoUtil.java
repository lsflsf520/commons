package com.yisi.stiku.rpc.util;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.yisi.stiku.common.utils.RegexUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcNode;

/**
 * 
 * @author shangfeng
 *
 */
public class ZkRpcNodeInfoUtil {

	private final static Logger LOG = LoggerFactory.getLogger(ZkRpcNodeInfoUtil.class);

	/**
	 * 
	 * @param group
	 * @param module
	 * @return 返回一条根据ZkConstant.ZK_DATA_PATH、group和module生成好的node注册zk路径
	 */
	public static String buildNodePath(String group, String module) {

		if (StringUtils.isEmpty(group)) {
			group = RpcConstants.DEFAULT_GRP;
		}
		if (StringUtils.isEmpty(module)) {
			module = RpcConstants.DEFAULT_MODULE;
		}

		return RpcConstants.ZK_NODE_PATH + "/" + group + "/" + module;
	}

	public static String buildNode(RpcNode rpcNode) {

		return rpcNode.getHost() + ":" + rpcNode.getPort();
	}

	/**
	 * 
	 * @param rpcNode
	 * @return
	 */
	public static String buildNodeProp(RpcNode rpcNode, Set<String> services) {

		StringBuilder builder = new StringBuilder();
		builder.append("host=");
		builder.append(rpcNode.getHost());
		builder.append("\nport=");
		builder.append(rpcNode.getPort());
		builder.append("\nweight=");
		builder.append(rpcNode.getWeight());
		builder.append("\nversion=");
		builder.append(rpcNode.getVersion());
		builder.append("\ngroup=");
		builder.append(rpcNode.getGroup());
		builder.append("\nmodule=");
		builder.append(rpcNode.getModule());
		builder.append("\nproject=");
		builder.append(ZkConstant.ALIAS_PROJECT_NAME);
		builder.append("\nservices=");
		builder.append(joinServices(services));

		return builder.toString();
	}

	private static String joinServices(Set<String> services) {

		StringBuilder builder = new StringBuilder();
		if (services != null) {
			for (String service : services) {
				builder.append(service);
				builder.append(",");
			}

			if (builder.length() > 0) {
				builder.setLength(builder.length() - 1);
			}
		}

		return builder.toString();
	}

	public static RpcNode parse2RpcNode(String nodePath) {

		RpcNode node = new RpcNode();
		node.setHost(ConfigOnZk.getValue(nodePath, "host"));
		String port = ConfigOnZk.getValue(nodePath, "port");
		if (RegexUtil.isInt(port)) {
			node.setPort(Integer.valueOf(port));
		} else {
			LOG.warn("port '" + port + "' is illegal or not set for node path '" + nodePath
					+ "' yet, and will use default port");
		}
		node.setVersion(ConfigOnZk.getValue(nodePath, "version"));
		String weight = ConfigOnZk.getValue(nodePath, "weight");
		if (RegexUtil.isInt(weight)) {
			node.setWeight(Integer.valueOf(weight));
		}
		node.setGroup(ConfigOnZk.getValue(nodePath, "group"));
		node.setModule(ConfigOnZk.getValue(nodePath, "module"));
		node.setProject(ConfigOnZk.getValue(nodePath, "project"));

		Set<String> services = new HashSet<String>();
		node.setServices(services);

		// String serviceStr = prop.getProperty("services");

		// if(!StringUtils.isEmpty(serviceStr)){
		String[] serviceArr = ConfigOnZk.getValueArr(nodePath, "services");
		if (serviceArr != null && serviceArr.length > 0) {
			for (String service : serviceArr) {
				services.add(service);
			}
		}
		// }

		return node;
	}

	// public static RpcNode parse2RpcNode(byte[] content){
	// ByteArrayInputStream in = new ByteArrayInputStream(content);
	// Properties prop = new Properties();
	// try {
	// prop.load(in);
	// RpcNode node = new RpcNode();
	// node.setHost(prop.getProperty("host"));
	// node.setPort(Integer.valueOf(prop.getProperty("port")));
	// node.setVersion(prop.getProperty("version"));
	// node.setWeight(Integer.valueOf(prop.getProperty("weight")));
	//
	// Set<String> services = new HashSet<String>();
	// node.setServices(services);
	//
	// String serviceStr = prop.getProperty("services");
	// if(!StringUtils.isEmpty(serviceStr)){
	// String[] serviceArr = serviceStr.split(",");
	// for(String service : serviceArr){
	// services.add(service);
	// }
	// }
	//
	// return node;
	// } catch (IOException e) {
	// LOG.error(e.getMessage(), e);
	// }
	//
	// return null;
	// }

	// public static void build2Service2NodeMap(RpcNode node,
	// Map<String/*service name*/, List<RpcNode>> service2NodeMap){
	// if(node == null || node.getServices() == null ||
	// node.getServices().isEmpty()){
	// if(node == null){
	// LOG.warn("param node cannot be null ");
	// }else{
	// LOG.warn("found not any service on server " + node.getHost() + ":" +
	// node.getPort());
	// }
	// return;
	// }
	//
	// for(String service : node.getServices()){
	// String serviceKey = buildServiceKey(service, node.getVersion());
	// List<RpcNode> nodeList = service2NodeMap.get(serviceKey);
	// if(nodeList == null){
	// nodeList = new ArrayList<RpcNode>();
	// service2NodeMap.put(serviceKey, nodeList);
	// }
	//
	// nodeList.add(node);
	// }
	// }

	// public static void parse2Service2NodeMap(byte[] content,
	// Map<String/*service name*/, List<RpcNode>> service2NodeMap){
	// ByteArrayInputStream in = new ByteArrayInputStream(content);
	// Properties prop = new Properties();
	// try {
	// prop.load(in);
	// RpcNode node = new RpcNode();
	// node.setHost(prop.getProperty("host"));
	// node.setPort(Integer.valueOf(prop.getProperty("port")));
	// node.setVersion(prop.getProperty("version"));
	// node.setWeight(Integer.valueOf(prop.getProperty("weight")));
	//
	// String serviceStr = prop.getProperty("services");
	// if(!StringUtils.isEmpty(serviceStr)){
	// String[] serviceArr = serviceStr.split(",");
	// for(String service : serviceArr){
	// String serviceKey = buildServiceKey(service, node.getVersion());
	// List<RpcNode> nodeList = service2NodeMap.get(serviceKey);
	// if(nodeList == null){
	// nodeList = new ArrayList<RpcNode>();
	// service2NodeMap.put(serviceKey, nodeList);
	// }
	//
	// nodeList.add(node);
	// if(node.getWeight() > 1){//权重如果大于1，则需要根据权重在nodeList中重复出现n次，以便随机算法能用上权重值
	// for(int i=1; i<node.getWeight(); i++){
	// nodeList.add(node);
	// }
	// }
	// }
	// }
	// } catch (IOException e) {
	// LOG.error(e.getMessage(), e);
	// }
	//
	// }

	/**
	 * 
	 * @param clazzName
	 * @param version
	 * @return 根据服务类名和版本号，生成唯一标识该服务的key
	 */
	public static String buildServiceKey(String clazzName, String version) {

		return clazzName + ":" + version;
	}
}
