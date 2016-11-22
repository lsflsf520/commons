package com.yisi.stiku.rpc.cluster.impl;

import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.client.netty.NettyClient;
import com.yisi.stiku.rpc.client.netty.ZkServiceDiscovery;
import com.yisi.stiku.rpc.cluster.AbstractRegistry;

public class NettyClientRegistry extends AbstractRegistry<NettyClient> implements InitializingBean, ApplicationContextAware {

	private final static Logger LOG = LoggerFactory.getLogger(NettyClientRegistry.class);

	private ZkServiceDiscovery discovery;

	private ApplicationContext applicationContext;

	// @Resource
	// private NettyClient nettyClient;

	public NettyClientRegistry(ZkServiceDiscovery discovery) {

		this.discovery = discovery;
	}

	@Override
	protected NettyClient getNewClientInstance() {

		return new NettyClient();
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		discovery.setRegistry(this);
		Set<RpcNode> rpcNodes = discovery.getOnServiceNodes();
		if (rpcNodes == null) {
			LOG.warn("there found no rpc nodes.");
			return;
		}

		Iterator<RpcNode> itr = rpcNodes.iterator();
		while (itr.hasNext()) {
			super.register(itr.next());
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		this.applicationContext = applicationContext;

	}

	public ApplicationContext getApplicationContext() {

		return applicationContext;
	}

}
