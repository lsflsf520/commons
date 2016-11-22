package com.yisi.stiku.rpc.server.netty.handler;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatRespHandler extends ChannelHandlerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(HeartBeatRespHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		RpcRequest request = (RpcRequest)msg;
		if("PING".equals(request.getMessageId())){
			InetSocketAddress clientAddr = (InetSocketAddress)ctx.channel().remoteAddress();
			LOG.debug("received heart beat message from " + clientAddr.getAddress().getHostAddress() + ":" + clientAddr.getPort() + " and response to it.");
			RpcResponse response = new RpcResponse();
			ctx.writeAndFlush(response);
		}else{
			ctx.fireChannelRead(msg);
		}
	}
	
}
