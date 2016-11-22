package com.yisi.stiku.rpc.server.netty.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcConstants;

@Component
public class NettyServerChannelInitializer extends
		ChannelInitializer<SocketChannel> {

	@Resource
	private NettyServerDispatchHandler nettyServerDispatchHandler;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		int interval = Integer.valueOf(ConfigOnZk.getValue(
				ZkConstant.APP_ZK_PATH, "rpc.heartbeat.interval",
				RpcConstants.DEFAULT_HEARTBEAT_SECONDS + ""));
		int maxHeartBeatFailCnt = Integer.valueOf(ConfigOnZk.getValue(
				ZkConstant.APP_ZK_PATH, "rpc.max.heartbeat.fail.cnt", "5"));
		ch.pipeline().addLast(
				new ReadTimeoutHandler(maxHeartBeatFailCnt * interval));
		ch.pipeline().addLast(new HeartBeatRespHandler());
		ch.pipeline().addLast(nettyServerDispatchHandler);

	}

}
