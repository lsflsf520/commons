package com.yisi.stiku.rpc.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;
import com.yisi.stiku.rpc.client.Client;

/**
 * 
 * @author shangfeng
 *
 */
public abstract class AbstractRouter implements Router {
	
	@Override
	public RpcResponse send(final RpcRequest request) {
		return send(request, null);
	}
	
	private RpcResponse send(final RpcRequest request, List<Client> hasTriedClients){
		Client client = getLoadBalance().select(request, hasTriedClients);
		try{
			return client.send(request);
		}catch(IOException e){
			if(hasTriedClients == null){
				hasTriedClients = new ArrayList<Client>();
			}
			hasTriedClients.add(client);
			
			return send(request, hasTriedClients);
		}
	}
	
	protected abstract LoadBalance getLoadBalance();
}
