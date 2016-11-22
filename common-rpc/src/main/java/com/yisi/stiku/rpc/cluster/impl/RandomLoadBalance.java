package com.yisi.stiku.rpc.cluster.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.client.Client;
import com.yisi.stiku.rpc.cluster.LoadBalance;
import com.yisi.stiku.rpc.cluster.Registry;

/**
 * 
 * @author shangfeng
 *
 */
public class RandomLoadBalance implements LoadBalance {

	private final static Logger LOG = LoggerFactory
			.getLogger(RandomLoadBalance.class);

	private final Registry<? extends Client> registry;

	/**
	 * 
	 * @param registry
	 */
	public RandomLoadBalance(final Registry<? extends Client> registry) {
		this.registry = registry;
	}

	@Override
	public Client select(RpcRequest request, List<Client> hasTriedClients) {
		List<? extends Client> registeredClients = registry
				.getRegisteredClients(request);
		if (registeredClients == null || registeredClients.isEmpty()) {
			throw new BaseRuntimeException("NO_SERVER_AVAIL", "服务异常，请重试或联系管理员", "no server available");
		}

		int maxTries = Integer.valueOf(ConfigOnZk.getValue(
				ZkConstant.APP_ZK_PATH, "rpc.client.tries.max", "3"));
		if (hasTriedClients == null) {
			hasTriedClients = new ArrayList<Client>();
		} else if (hasTriedClients.size() >= maxTries) {
			throw new BaseRuntimeException("MAX_TRIED_RPC", "服务异常，请重试或联系管理员", "has tried " + maxTries + " times, then abort it.");
		}
		
		//如果没有更多的服务器可用来重试，则直接抛出异常
		if(!hasTriedClients.isEmpty() && !hasMoreClient(registeredClients, hasTriedClients)){
			throw new BaseRuntimeException("NO_MORE_SERVER", "服务异常，请重试或联系管理员", "no more server can be used to try");
		}

		int selected = new Random().nextInt(registeredClients.size());
		Client client = registeredClients.get(selected);

		for (Client hasTried : hasTriedClients) {
			if (client.getRemoteAddress().equals(hasTried.getRemoteAddress())) {
				client = select(request, hasTriedClients);
				break;
			}
		}

		LOG.debug(LOG.isDebugEnabled() ? ("request id '"
				+ request.getMessageId() + "' call method '"
				+ request.getApiClassName() + "." + request.getMethod()
				+ "' with version '" + request.getServiceVersion() + "' on "
				+ client.getRemoteAddress().getAddress().getHostAddress() + ":" + client
				.getRemoteAddress().getPort()) : null);

		return client;
	}
	
	private boolean hasMoreClient(List<? extends Client> registeredClients, List<? extends Client> hasTriedClients){
		Set<String> registeredHostIPs = parseClients(registeredClients);
		Set<String> hasTriedHostIPs = parseClients(hasTriedClients);
		
		return registeredHostIPs.size() > 0 && (registeredHostIPs.size() > hasTriedHostIPs.size() || !registeredHostIPs.toString().equals(hasTriedHostIPs.toString())) ;
	}
	
	private Set<String> parseClients(List<? extends Client> clients){
		SortedSet<String> hostips = new TreeSet<String>();
		for(Client client : clients){
			hostips.add(client.getRemoteAddress().toString());
		}
		
		return Collections.unmodifiableSortedSet(hostips);
	}

}
