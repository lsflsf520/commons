package com.yisi.stiku.rpc.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;

/**
 * 
 * @author shangfeng
 *
 */
public interface Client {
	
	/**
	 * 
	 * @param socketAddress
	 */
	void connect(InetSocketAddress socketAddress);
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	RpcResponse send(RpcRequest request) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	InetSocketAddress getRemoteAddress();
	
	/**
	 * 
	 */
	void close();
	
	/**
	 * 
	 * @return 客户端是否可用
	 */
	boolean isActive();
}
