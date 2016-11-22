package com.yisi.stiku.rpc.client.netty.handler;

import io.netty.channel.socket.SocketChannel;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.yisi.stiku.rpc.codec.KryoDecoder;
import com.yisi.stiku.rpc.codec.KryoEncoder;
import com.yisi.stiku.rpc.codec.KryoPool;


@Component
public class KryoNettyClientChannelInitializer extends NettyClientChannelInitializer {
	
	
	@Resource
	private KryoPool kryoSerializationFactory;
	
	@Override
	protected void initChannel(final SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new KryoEncoder(kryoSerializationFactory));
		ch.pipeline().addLast(new KryoDecoder(kryoSerializationFactory));
		super.initChannel(ch);
	}
}
