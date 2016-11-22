package com.yisi.stiku.rpc.client.netty.handler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;

public class HeartBeatReqHandler extends ChannelHandlerAdapter {
	
	private final static Logger LOG = LoggerFactory.getLogger(HeartBeatReqHandler.class);

	private volatile ScheduledFuture<?> heartBeat;
	private Object initLock = new Object();
	
	public void channelRead(ChannelHandlerContext ctx, Object msg){
		RpcResponse response = (RpcResponse)msg;
		if("PONG".equals(response.getMessageId())){
			InetSocketAddress remoteAddr = (InetSocketAddress)ctx.channel().remoteAddress();
			LOG.debug("received heart beat response from " + remoteAddr.getAddress().getHostAddress() + ":" + remoteAddr.getPort());
		}else{
			ctx.fireChannelRead(msg);
		}
		
		if(heartBeat == null || heartBeat.isCancelled()){
			synchronized (initLock) {
				if(heartBeat == null || heartBeat.isCancelled()){
				  int interval = Integer.valueOf(ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "rpc.heartbeat.interval", RpcConstants.DEFAULT_HEARTBEAT_SECONDS + ""));
				  heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 5, interval, TimeUnit.SECONDS);
				  LOG.debug("start heartbeat check thread with interval " + interval + " seconds.");
				}
			}
		}
		
	}
	
	private class HeartBeatTask implements Runnable{

		private final ChannelHandlerContext ctx;
		
		public HeartBeatTask(ChannelHandlerContext ctx){
			this.ctx = ctx;
		}
		
		@Override
		public void run() {
			RpcRequest request = new RpcRequest();
			InetSocketAddress remoteAddr = (InetSocketAddress)ctx.channel().remoteAddress();
			LOG.debug("send heart beat message to "+remoteAddr.getAddress().getHostAddress() + ":" + remoteAddr.getPort());
			ctx.writeAndFlush(request);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if(heartBeat != null){
			heartBeat.cancel(true);
			heartBeat = null;
		}
		ctx.fireExceptionCaught(cause);
	}
	
}
