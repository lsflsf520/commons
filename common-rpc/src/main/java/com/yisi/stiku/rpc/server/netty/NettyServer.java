package com.yisi.stiku.rpc.server.netty;

import java.util.HashMap;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.conf.BaseConfig;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.rpc.annotation.RpcService;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.exception.ServerStopException;
import com.yisi.stiku.rpc.serial.SerializeType;
import com.yisi.stiku.rpc.server.Server;

@Component
public class NettyServer implements Server, ApplicationContextAware, InitializingBean {
	
	private final static Logger LOG = LoggerFactory.getLogger(NettyServer.class);
	
	private ApplicationContext applicationContext;
	
	private int bossGroupThreads;
	
	private int workerGroupThreads;
	
	private int backlogSize;
	
	private SerializeType serializeType;
	
	private RpcNode rpcNode;
	
	private Channel channel;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private ZkRegistry zkRegistry;
	
	private transient boolean needStart = false;
	
	private final Map<String/*rpc service interface name*/, Object> handlerMap = new HashMap<String, Object>(); // 存放接口名与服务对象之间的映射关系
	
	public NettyServer() {
		String needServerStart = BaseConfig.getValue("rpc.server.start.needed", "false");
		if("true".equals(needServerStart)){
			needStart = true;
		}
		
		if(!needStart){
			return;
		}
		
		bossGroupThreads = Integer.valueOf(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "server.boss.group.threads", "5"));
		workerGroupThreads = Integer.valueOf(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "server.worker.group.threads", "10"));
		backlogSize = Integer.valueOf(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "server.backlog.size", "128"));
		serializeType = SerializeType.valueOf(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "serializer.type", "Kryo"));
		
		rpcNode = new RpcNode();
		rpcNode.setGroup(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "rpc.node.group", RpcConstants.DEFAULT_GRP));
		rpcNode.setModule(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "rpc.node.module", RpcConstants.DEFAULT_MODULE));
		rpcNode.setHost(RpcConstants.getRpcHost());
		rpcNode.setPort(RpcConstants.getRpcPort());
		rpcNode.setWeight(Integer.valueOf(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "rpc.node.weight", "1")));
		rpcNode.setVersion(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "rpc.node.version", "1.0.0"));
		
		zkRegistry = new ZkRegistry();
	}
	
	@Override
	public void start() {
		bossGroup = new NioEventLoopGroup(bossGroupThreads);
		workerGroup = new NioEventLoopGroup(workerGroupThreads);
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, backlogSize)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childHandler(applicationContext.getBean(serializeType.getServerChannelInitializer()));
		try {
			channel = serverBootstrap.bind(rpcNode.getHost(), rpcNode.getPort()).sync().channel();
			
			zkRegistry.register(rpcNode, handlerMap.keySet());
			LOG.debug("server started at " + rpcNode.getHost() + ":" + rpcNode.getPort());
		} catch (final InterruptedException ex) {
			throw new BaseRuntimeException("RPC_SERVER_ERROR", "远程服务启动失败", "start rpc server error", ex);
		}
	}
	
	@Override
	public void stop() {
		if (null == channel) {
			throw new ServerStopException();
		}
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		channel.closeFuture().syncUninterruptibly();
		bossGroup = null;
		workerGroup = null;
		channel = null;
	}
	
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		if(!needStart){
			return;
		}
		this.applicationContext = applicationContext;
		
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class); // 获取所有带有 RpcService 注解的 Spring Bean
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
            	Object targetBean = serviceBean;
            	if(serviceBean instanceof Advised){
            		Advised advised = (Advised)serviceBean;
            		SingletonTargetSource singTarget = (SingletonTargetSource) advised.getTargetSource();
            		targetBean = singTarget.getTarget();
            	}
            	Class<?> interfze = targetBean.getClass().getAnnotation(RpcService.class).value();
            	String interfaceName = targetBean.getClass().getName();
            	if(interfze != null && !Object.class.equals(interfze)){
            		interfaceName = interfze.getName();
            	}
            	
            	if(interfaceName.endsWith("ServiceImpl")){
            		if(interfaceName.endsWith("RpcServiceImpl")){
            			interfaceName = interfaceName.substring(0, interfaceName.length() - "Impl".length());
            		}else{
            			interfaceName = interfaceName.replace("ServiceImpl", "RpcService");
            		}
            		interfaceName = interfaceName.replace(".service.impl.", ".rpc.service.");
            	}
            	
                handlerMap.put(interfaceName, serviceBean);
            }
        }
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(needStart){
			StartRpcServerRunner runner = new StartRpcServerRunner();
			runner.start();
		}
	}
	
	private class StartRpcServerRunner extends Thread{
		
		@Override
		public void run() {
			NettyServer.this.start();
		}
		
	}
}
