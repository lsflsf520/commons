package com.yisi.stiku.rpc.cluster;

import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;

public interface Router {
	
	RpcResponse send(RpcRequest request);
}
