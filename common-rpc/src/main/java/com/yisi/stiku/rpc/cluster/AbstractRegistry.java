package com.yisi.stiku.rpc.cluster;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.yisi.stiku.common.bean.ConcurrentHashSet;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.client.Client;
import com.yisi.stiku.rpc.client.netty.NettyClient;
import com.yisi.stiku.rpc.util.ZkRpcNodeInfoUtil;

public abstract class AbstractRegistry<T extends Client> implements Registry<T> {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractRegistry.class);

	// private final List<T> clients = new ArrayList<T>();
	private final Set<RpcNode> registeredRpcNodes = new ConcurrentHashSet<RpcNode>();
	private final Map<String, List<T>> service2ClientMap = new ConcurrentHashMap<String, List<T>>();

	@Override
	public synchronized void register(final RpcNode rpcNode) {

		if (hasRegistered(rpcNode)) {
			return;
		}
		if (rpcNode.getServices() == null || rpcNode.getServices().isEmpty()) {
			LOG.warn("not found any service on server " + rpcNode.getHost() + ":" + rpcNode.getPort());
			return;
		}

		try {
			T client = getNewClientInstance();

			if (client instanceof NettyClient) {
				((NettyClient) client).setApplicationContext(getApplicationContext());
			}
			client.connect(new InetSocketAddress(rpcNode.getHost(), rpcNode.getPort()));

			registeredRpcNodes.add(rpcNode);
			for (String service : rpcNode.getServices()) {
				String serviceKey = ZkRpcNodeInfoUtil.buildServiceKey(service, rpcNode.getVersion());
				List<T> clientList = service2ClientMap.get(serviceKey);
				if (clientList == null) {
					clientList = new ArrayList<T>();
					service2ClientMap.put(serviceKey, clientList);
				}

				clientList.add(client);
				if (rpcNode.getWeight() > 1) {// 权重如果大于1，则需要根据权重在nodeList中重复出现n次，以便随机算法能用上权重值
					for (int i = 1; i < rpcNode.getWeight(); i++) {
						clientList.add(client);
					}
				}
			}
		} catch (Exception ex) {
			LOG.warn(rpcNode.getHost() + ":" + rpcNode.getPort() + " connect failure. errorMsg:" + ex.getMessage());
		}
	}

	@Override
	public synchronized void unregister(final RpcNode rpcNode) {

		if (!hasRegistered(rpcNode)) {
			return;
		}
		registeredRpcNodes.remove(rpcNode);
		if (rpcNode.getServices() != null && !rpcNode.getServices().isEmpty()) {
			// 将节点中的服务注销掉
			for (String service : rpcNode.getServices()) {
				String serviceKey = ZkRpcNodeInfoUtil.buildServiceKey(service, rpcNode.getVersion());
				List<T> clients = service2ClientMap.get(serviceKey);
				List<T> newClients = new ArrayList<T>();
				if (clients != null && !clients.isEmpty()) {
					for (T client : clients) {
						if (!client.getRemoteAddress().getAddress().getHostAddress().equals(rpcNode.getHost())
								|| client.getRemoteAddress().getPort() != rpcNode.getPort()) {
							newClients.add(client);
						} else {
							if (client.isActive()) {
								try {
									LOG.debug("close rpc client(" + rpcNode.getHost() + ":" + rpcNode.getPort() + ")");
									client.close();
								} catch (Exception e) {
									LOG.error(
											"error occured when close client(" + rpcNode.getHost() + ":" + rpcNode.getPort()
													+ ")", e);
								}
							}
						}
					}
				}

				service2ClientMap.put(serviceKey, newClients);
			}
		}
	}

	private boolean hasRegistered(final RpcNode rpcNode) {

		return registeredRpcNodes.contains(rpcNode);
	}

	@Override
	public Set<RpcNode> getAllRpcNodes() {

		return Collections.unmodifiableSet(registeredRpcNodes);
	}

	@Override
	public List<T> getRegisteredClients(RpcRequest request) {

		String serviceKey = ZkRpcNodeInfoUtil.buildServiceKey(request.getApiClassName(), request.getServiceVersion());
		List<T> clients = service2ClientMap.get(serviceKey);
		if (clients == null || clients.isEmpty()) {
			throw new BaseRuntimeException("NO_SERVER", "服务异常，请重试或联系管理员！", "not found any server for rpc service '"
					+ request.getApiClassName() + "' with version '" + request.getServiceVersion() + "'");
		}
		return Collections.unmodifiableList(clients);
	}

	@Override
	public Set<String> loadCurrServices() {

		Set<String> services = service2ClientMap.keySet();
		return services == null ? new HashSet<String>() : Collections.unmodifiableSet(services);
	}

	@Override
	public List<RpcNode> loadNodes4Service(String serviceKey) {

		List<RpcNode> rpcNodes = new ArrayList<RpcNode>();
		if (StringUtils.isNotBlank(serviceKey)) {
			for (RpcNode node : registeredRpcNodes) {
				if (node.getServices() == null) {
					continue;
				}
				for (String service : node.getServices()) {
					if (serviceKey.equals(ZkRpcNodeInfoUtil.buildServiceKey(service, node.getVersion()))) {
						rpcNodes.add(node);
						break;
					}
				}
			}
		}

		return Collections.unmodifiableList(rpcNodes);
	}

	protected abstract T getNewClientInstance();

	protected abstract ApplicationContext getApplicationContext();

}
