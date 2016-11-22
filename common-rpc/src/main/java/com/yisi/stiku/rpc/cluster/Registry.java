package com.yisi.stiku.rpc.cluster;

import java.util.List;
import java.util.Set;

import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.client.Client;


public interface Registry<T extends Client> {
	
	void register(RpcNode rpcNode);
	
	void unregister(RpcNode rpcNode);
	
	List<T> getRegisteredClients(RpcRequest request);
	
	Set<RpcNode> getAllRpcNodes();
	
	Set<String> loadCurrServices();
	
	List<RpcNode> loadNodes4Service(String serviceKey);
	
}
