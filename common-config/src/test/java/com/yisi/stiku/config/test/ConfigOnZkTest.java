package com.yisi.stiku.config.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yisi.stiku.lock.DistribLock;
import com.yisi.stiku.lock.LockContext;

public class ConfigOnZkTest {

	public static void main(String[] args) throws InterruptedException {

		// String url =
		// ConfigOnZk.getValue("/csjy/redis/defaultgrp/r/test.properties",
		// "jdbc.url");
		//
		// System.out.println(url);
		//
		// Thread.sleep(300000);

		// ZkClient zkClient = new ZkClient("127.0.0.1:2181", 2000);

		// ConfigOnZk.createEphemeralNode("/csjy/distrib_lock/defaultgrp/defaultmod/1234",
		// "wa haha");
		//
		// ConfigOnZk.createEphemeralNode("/csjy/distrib_lock/defaultgrp/defaultmod/1234",
		// "wa haha2");

		ExecutorService exec = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 20; i++) {
			exec.submit(new Runnable() {

				@Override
				public void run() {

					LockContext context = DistribLock.lock4Onetime("werwe");
					if (context.isLockSuccess()) {
						System.out.println(context);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					DistribLock.unlock4Onetime(context);

					// DistribLock.lock4Onetime("student", 1234, new
					// LockExecService() {
					//
					// @Override
					// public void execute(LockContext context) {
					//
					// System.out.println("I have the locked, context:" +
					// context);
					// try {
					// Thread.sleep(20000l);
					// } catch (InterruptedException e) {
					// e.printStackTrace();
					// }
					//
					// }
					// });

				}
			});

			// Iterator<String> keyItr =
			// ConfigOnZk.getKeys("common/flowlimit.xml");
			// while (keyItr.hasNext()) {
			// System.out.println(keyItr.next());
			// }
		}
	}
}
