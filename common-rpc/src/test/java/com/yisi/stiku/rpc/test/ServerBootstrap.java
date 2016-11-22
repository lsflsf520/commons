package com.yisi.stiku.rpc.test;

import java.lang.reflect.InvocationTargetException;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yisi.stiku.rpc.server.netty.NettyServer;


public final class ServerBootstrap {
	
	private static AbstractApplicationContext ctx;
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		SpringContainer container = startContainer();
//		ctx = container.getContext().get();
//		startServer();
		
		ctx = new ClassPathXmlApplicationContext("classpath:spring/spring-rpc-service.xml");
		
//		Object apiInstance = ctx.getBean(HelloRpcService.class);
//		Method method = apiInstance.getClass().getMethod("sayHello", new Class<?>[]{String.class, Integer.class});
//		Object result = method.invoke(apiInstance, new Object[]{"hshs", 2});
//		
//		System.out.println(result);
		
//		FastClass serviceFastClass = FastClass.create(HelloRpcService.class);
//        FastMethod serviceFastMethod = serviceFastClass.getMethod("sayHello", new Class<?>[]{String.class, Integer.class});
//         
//        Object result = serviceFastMethod.invoke(apiInstance, new Object[]{"hshs", 2});
//        System.out.println(result);
		
//		startServer();
	}
	
//	private static SpringContainer startContainer() {
//		SpringContainer container = new SpringContainer();
//		container.start();
//		return container;
//	}
	
	private static void startServer() {
//		Object port = ctx.getBean(PropertySourcesPlaceholderConfigurer.class).getAppliedPropertySources().get("localProperties").getProperty("server.port");
		ctx.getBean(NettyServer.class).start();
	}
	
	public static void stopServer() {
		ctx.getBean(NettyServer.class).stop();
	}
}
