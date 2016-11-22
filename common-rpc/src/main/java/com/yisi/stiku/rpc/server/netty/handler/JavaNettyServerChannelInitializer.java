package com.yisi.stiku.rpc.server.netty.handler;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import org.springframework.stereotype.Component;

@Component
public class JavaNettyServerChannelInitializer extends NettyServerChannelInitializer {
	
//	@Resource
//	private NettyServerDispatchHandler serverDispatchHandler;
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new ObjectEncoder());
		ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
//		ch.pipeline().addLast(serverDispatchHandler);
		super.initChannel(ch);
	}
}
