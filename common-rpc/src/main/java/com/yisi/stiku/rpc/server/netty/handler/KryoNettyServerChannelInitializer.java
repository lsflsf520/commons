package com.yisi.stiku.rpc.server.netty.handler;

import io.netty.channel.socket.SocketChannel;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.yisi.stiku.rpc.codec.KryoDecoder;
import com.yisi.stiku.rpc.codec.KryoEncoder;
import com.yisi.stiku.rpc.codec.KryoPool;

@Component
public class KryoNettyServerChannelInitializer extends NettyServerChannelInitializer {
	
//	@Resource
//	private NettyServerDispatchHandler serverDispatchHandler;
	
	@Resource
	private KryoPool kryoSerializationFactory;
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new KryoEncoder(kryoSerializationFactory));
		ch.pipeline().addLast(new KryoDecoder(kryoSerializationFactory));
//		ch.pipeline().addLast(serverDispatchHandler);
		super.initChannel(ch);
	}
}
