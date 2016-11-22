package com.yisi.stiku.rpc.cluster;

import java.util.List;

import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.client.Client;

public interface LoadBalance {
	
	/**
	 * 
	 * @param request 
	 * @param hasTriedClients 已经重试过的连接列表(如果没有，可以传null)
	 * @return 从可用的连接中挑选出一个不存在hasTriedClients列表中的连接并返回
	 */
	Client select(RpcRequest request, List<Client> hasTriedClients);
	
}
