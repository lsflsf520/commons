package com.yisi.stiku.rpc.cluster.impl;

import com.yisi.stiku.rpc.cluster.AbstractRouter;
import com.yisi.stiku.rpc.cluster.LoadBalance;

public class NettyClientRouter extends AbstractRouter {
	
	private LoadBalance loadBalance;

	@Override
	protected LoadBalance getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(LoadBalance loadBalance) {
		this.loadBalance = loadBalance;
	}
	
}
