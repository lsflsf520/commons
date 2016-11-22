package com.yisi.stiku.rpc.client.netty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.github.zkclient.AbstractListener;
import com.yisi.stiku.common.bean.ConcurrentHashSet;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.client.Client;
import com.yisi.stiku.rpc.cluster.Registry;
import com.yisi.stiku.rpc.util.ZkRpcNodeInfoUtil;

public class ZkServiceDiscovery implements InitializingBean {

	private static final Logger LOG = LoggerFactory
			.getLogger(ZkServiceDiscovery.class);

	private Map<String/* group */, Set<String/* module */>> grpModelMap;

	private boolean ignoreDefaultGrp;

	private boolean ignoreDefaultMod;

	private ConcurrentHashSet<RpcNode> rpcNodes = new ConcurrentHashSet<RpcNode>();

	private Registry<? extends Client> registry;

	private AbstractListener zkChgListener = new AbstractListener() {

		@Override
		public void handleStateChanged(KeeperState state)
				throws Exception {

			LOG.warn("zk state changed to " + state);
		}

		@Override
		public void handleChildChange(String parentPath,
				List<String> currentChildren)
				throws Exception {

			LOG.debug("rpc node has changed under zk node {}", parentPath);
			watchNode(false);
		}

		@Override
		public void handleNewSession() throws Exception {

			LOG.debug("rediscover rpc service on zk. ");
			watchNode(false);
		}
	};

	public ZkServiceDiscovery() {

	}

	private void watchNode(boolean isInit) {

		Set<RpcNode> currRpcNodes = new HashSet<RpcNode>();
		Set<String> groups = grpModelMap.keySet();
		for (String group : groups) {
			Set<String> modules = grpModelMap.get(group);
			if (modules != null) {
				for (String module : modules) {
					if (RpcConstants.DEFAULT_MODULE.equals(module) && ignoreDefaultMod) {
						continue; // 如果需要强制忽略掉DEFAULT_MODULE下的节点，则继续下一个循环
					}
					String zkNodePath = ZkRpcNodeInfoUtil.buildNodePath(
							group, module);
					List<String> zkNodeList = null;
					if (isInit) {
						zkNodeList = ConfigOnZk.subscribeChildChanges(zkNodePath, zkChgListener);
						ConfigOnZk.subscribeStateChanges(zkChgListener);
					} else {
						zkNodeList = ConfigOnZk.getChildren(zkNodePath);
					}

					List<String> onServiceNodeList = new ArrayList<String>();
					if (zkNodeList != null && !zkNodeList.isEmpty()) {
						for (String node : zkNodeList) {
							RpcNode rpcNode = ZkRpcNodeInfoUtil
									.parse2RpcNode(zkNodePath + "/" + node);

							if (!isSelf(rpcNode)) {
								onServiceNodeList.add(node);
								currRpcNodes.add(rpcNode);

								if (registry != null) {
									registry.register(rpcNode);
								}
							}
						}
					}

					LOG.debug(LOG.isDebugEnabled() ? "on service nodes for group '"
							+ group
							+ "' and module '"
							+ module
							+ "': "
							+ onServiceNodeList
							: "");
				}
			}
		}

		synchronized (ZkServiceDiscovery.class) {
			rpcNodes.clear();
			rpcNodes.addAll(currRpcNodes);

			if (registry != null && registry.getAllRpcNodes() != null
					&& !registry.getAllRpcNodes().isEmpty()) {
				List<RpcNode> offlineNodes = new ArrayList<RpcNode>();
				for (RpcNode preRpcNode : registry.getAllRpcNodes()) {
					if (!rpcNodes.contains(preRpcNode)) {
						offlineNodes.add(preRpcNode);
					}
				}

				for (RpcNode offlineNode : offlineNodes) {
					registry.unregister(offlineNode);
					LOG.warn("rpc node " + offlineNode.getHost() + ":"
							+ offlineNode.getPort()
							+ " is already offline.");
				}
			}
		}
	}

	/**
	 * 
	 * @param rpcNode
	 * @return 如果rpc服务节点与当前jvm自身的ip和端口一致，则返回true
	 */
	private boolean isSelf(RpcNode rpcNode) {

		return ZkConstant.ALIAS_PROJECT_NAME.equals(rpcNode.getProject())
		// && RpcConstants.getRpcHost().equals(rpcNode.getHost()) &&
		// RpcConstants.getRpcPort() == rpcNode.getPort()
		;
	}

	public void setGrpModelMap(Map<String, Set<String>> grpModelMap) {

		this.grpModelMap = grpModelMap;
	}

	/**
	 * 
	 * @return 返回当前正在服务的节点列表
	 */
	public Set<RpcNode> getOnServiceNodes() {

		return Collections.unmodifiableSet(rpcNodes);
	}

	public void setRegistry(Registry<? extends Client> registry) {

		this.registry = registry;
	}

	public void setIgnoreDefaultGrp(boolean ignoreDefaultGrp) {

		this.ignoreDefaultGrp = ignoreDefaultGrp;
	}

	public void setIgnoreDefaultMod(boolean ignoreDefaultMod) {

		this.ignoreDefaultMod = ignoreDefaultMod;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (grpModelMap == null) {
			grpModelMap = new HashMap<String, Set<String>>();
		}

		if (!ignoreDefaultGrp) {
			Set<String> defaultModules = grpModelMap
					.get(RpcConstants.DEFAULT_GRP);
			if (defaultModules == null) {
				defaultModules = new HashSet<String>();
				grpModelMap.put(RpcConstants.DEFAULT_GRP, defaultModules);
			}

			if (!ignoreDefaultMod) {
				defaultModules.add(RpcConstants.DEFAULT_MODULE);
			}
		}

		watchNode(true);
	}

}
