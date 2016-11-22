package com.yisi.stiku.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shangfeng
 *
 */
public class TaskUtil {

	private final static ExecutorService execServ = Executors.newCachedThreadPool();

	public static void exec(Runnable task) {

		execServ.execute(task);
	}

}
