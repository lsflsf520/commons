package com.yisi.stiku.rpc.client.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.springframework.context.ApplicationContext;

import com.esotericsoftware.minlog.Log;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;
import com.yisi.stiku.rpc.client.Client;
import com.yisi.stiku.rpc.serial.SerializeType;


//@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NettyClient implements Client{
	
	private ApplicationContext applicationContext;
	
	private int workerGroupThreads;
	
	private SerializeType serializeType;
	
	private EventLoopGroup workerGroup;
	private Channel channel;
	
//	@Resource
//	private KryoNettyClientChannelInitializer kryoNettyClientChannelInitializer;
	
	private InetSocketAddress socketAddress;
	
//	private long lastAccessTime = System.currentTimeMillis(); //记录上一次调用该客户端实例的时间，以便判断是否需要重连（临时方案）
	private Object connectLock = new Object(); //用来在初始化客户端的时候加锁
	
	public NettyClient() {
		workerGroupThreads = Integer.valueOf(ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "client.worker.group.threads", "5"));
		serializeType = SerializeType.valueOf(ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "serializer.type", "Kryo"));
	}
	
	@Override
	public void connect(final InetSocketAddress socketAddress) {
		workerGroup = new NioEventLoopGroup(workerGroupThreads);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap
			.group(workerGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(applicationContext.getBean(serializeType.getClientChannelInitializer()));
//			.handler(kryoNettyClientChannelInitializer);
		channel = bootstrap.connect(socketAddress.getAddress().getHostAddress(), socketAddress.getPort()).syncUninterruptibly().channel();
		this.socketAddress = socketAddress;
		
		channel.writeAndFlush(new RpcRequest()); //发送心跳包，触发心跳检测
	}
	
	@Override
	public RpcResponse send(final RpcRequest request) throws IOException {
		checkChannelAvail(); //先检查channel是否可用，如果channel不可用，则自动尝试重连
		
		channel.writeAndFlush(request);
		return applicationContext.getBean(serializeType.getClientChannelInitializer()).getResponse(request.getMessageId());
	}
	
	@Override
	public InetSocketAddress getRemoteAddress() {
		return this.socketAddress;
	}
	
	private void checkChannelAvail(){
		if(channel == null ){
			synchronized (connectLock) {
				if(channel == null){
					connect(socketAddress);
				}
			}
		}
		
		if(!isActive() 
				){
			synchronized (connectLock) {
				if(!isActive() 
						){
					close();
					connect(socketAddress);
				}
			}
		}
	}
	
	@Override
	public void close() {
		if (null == channel) {
			Log.warn("channel is null, it do not need to close.");
		}else{
			workerGroup.shutdownGracefully();
			channel.closeFuture().syncUninterruptibly();
			workerGroup = null;
			channel = null;
		}
	}
	
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public boolean isActive() {
		return channel != null && channel.remoteAddress() != null && channel.isActive() && channel.isOpen();
	}
}
