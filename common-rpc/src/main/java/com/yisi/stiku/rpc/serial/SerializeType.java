package com.yisi.stiku.rpc.serial;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import com.yisi.stiku.rpc.client.netty.handler.JavaNettyClientChannelInitializer;
import com.yisi.stiku.rpc.client.netty.handler.KryoNettyClientChannelInitializer;
import com.yisi.stiku.rpc.client.netty.handler.NettyClientChannelInitializer;
import com.yisi.stiku.rpc.server.netty.handler.JavaNettyServerChannelInitializer;
import com.yisi.stiku.rpc.server.netty.handler.KryoNettyServerChannelInitializer;


public enum SerializeType {
	
	Java(JavaNettyServerChannelInitializer.class, JavaNettyClientChannelInitializer.class),
	Kryo(KryoNettyServerChannelInitializer.class, KryoNettyClientChannelInitializer.class);
	
	private final Class<? extends ChannelInitializer<SocketChannel>> serverChannelInitializer;
	private final Class<? extends NettyClientChannelInitializer> clientChannelInitializer;
	
	private SerializeType(final Class<? extends ChannelInitializer<SocketChannel>> serverChannelInitializer, final Class<? extends NettyClientChannelInitializer> clientChannelInitializer) {
		this.serverChannelInitializer = serverChannelInitializer;
		this.clientChannelInitializer = clientChannelInitializer;
	}
	
	public Class<? extends ChannelInitializer<SocketChannel>> getServerChannelInitializer() {
		return serverChannelInitializer;
	}
	
	public Class<? extends NettyClientChannelInitializer> getClientChannelInitializer() {
		return clientChannelInitializer;
	}
}
