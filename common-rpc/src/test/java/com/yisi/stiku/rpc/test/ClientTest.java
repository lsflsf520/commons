package com.yisi.stiku.rpc.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientTest {

	public static void main(String[] args) throws BeansException, Throwable {

		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"classpath:spring/spring-rpc-client.xml");
		// Client client = ctx.getBean(NettyClient.class);
		// client.connect(new InetSocketAddress("192.168.100.105", 9097));
		//
		// RpcRequest request = new RpcRequest(HelloRpcService.class, "1.0.0",
		// "sayHello", "lsf", 2000);
		// RpcResponse response = client.sent(request);
		//
		// System.out.println(response.getReturnValue().toString().length() +
		// "," + response.getMessageId() + "," + response.getException());
		//
		// client.close();

		final HelloRpcService helloService =
				(HelloRpcService) ctx.getBean("helloService");
		// helloService.sayHello("sdfsf", 10);

		// Person p = helloService.getPerson();
		// System.out.println(p.getHeight() + "," + p.getName() + "," +
		// p.getWeight());

		// Car car = helloService.showCar();
		//
		// System.out.println(car.getPinpai() + "," + car.getHeight() + "," +
		// car.getLength() + "," + car.getWidth());

		// Response res = helloService.query(2);

		/*
		 * System.out.println(res.get_results().getNumFound() + "," +
		 * res.get_results().getStart() + "," +
		 * res.get_results().getMaxScore());
		 */

		// System.out.println(res.getSdl().getNumFound() + "," +
		// res.getSdl().getStart() + ","
		// + res.getSdl().getMaxScore());

		int count = 2000;
		ExecutorService exec = Executors.newFixedThreadPool(count);

		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			// exec.execute(new Runnable() {
			//
			// @Override
			// public void run() {

			String result = helloService.sayHello("HI", 2000);

			// }
			// });

		}

		exec.shutdown();

		exec.awaitTermination(100, TimeUnit.SECONDS);
		System.out.println("total time:" + (System.currentTimeMillis() - start));
		// System.out.println(RandomUtil.randomNumCode(1024));

		// boolean success = helloService.save(new TblSysMenu());

		// System.out.println(success);

		// boolean value =
		// RpcClientUtil.sendAsyncRequest((NettyClientRouter)ctx.getBean("nettyClientRouter"),
		// HelloRpcService.class.getName(), "1.0.0", "sayHello", new
		// Object[]{"yangming", 20}, new Class<?>[]{String.class, int.class});

		// AsynRpcRequest as = AsynRpcRequest.newInstance(
		// "com.yisi.stiku.priv.rpc.service.RoleMgrRpcService", "1.0.0",
		// "findByUserId", new Object[] { 1L },
		// new Class<?>[] { long.class }).buildCallBackRequest(false,
		// "com.yisi.stiku.rpc.test.HelloRpcService", "sayHello",
		// new Object[] { "jack", 20 },
		// new Class<?>[] { String.class, int.class });
		//
		// boolean value = RpcClientUtil.sendAsyncRequest(
		// (NettyClientRouter) ctx.getBean("nettyClientRouter"), as);
		// System.out.println(value);

		// RpcAssistService service =
		// (RpcAssistService)ctx.getBean("rpcAssistService");
		// Set<String> services = service.searchServices("PayCheckRpcService");
		//
		// System.out.println(services);
	}

}
