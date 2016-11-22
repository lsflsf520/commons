package com.yisi.stiku.rpc.test;

import java.util.Date;
import java.util.concurrent.locks.LockSupport;

/**
 * @author shangfeng
 *
 */
public class LockSupportTest extends Thread {

	static LockSupportTest thread2;

	private Thread currentThread;

	public LockSupportTest() {

	}

	public LockSupportTest(Thread currentThread) {

		this.currentThread = currentThread;
	}

	public static void main(String[] args) {

		// thread1 = new LockSupportSon();
		// thread1.start();
		// thread2 = new LockSupportTest(Thread.currentThread());
		// thread2.start();
		// testPark();

		System.out.println(System.currentTimeMillis());

	}

	public static void testPark() {

		System.out.println("[" + new Date() + "]before>>>>>>");
		LockSupport.park(Thread.currentThread());
		System.out.println("[" + new Date() + "]after>>>>>>>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {
			System.out.println("thread2 before>>>>");
			Thread.sleep(5000L);
			LockSupport.unpark(currentThread);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
