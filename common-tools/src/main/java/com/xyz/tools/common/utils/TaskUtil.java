package com.xyz.tools.common.utils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.intf.ITask;

/**
 * @author shangfeng
 *
 */
public class TaskUtil {

	private final static ExecutorService execServ = Executors.newFixedThreadPool(Integer.valueOf(BaseConfig.getValue("thread.pool.size", "10")));
	
	private final static ScheduledExecutorService scheduleServ = Executors.newScheduledThreadPool(Integer.valueOf(BaseConfig.getValue("thread.schedule.pool.size", "10")));

	private final static Map<String, ITask> taskMap = new ConcurrentHashMap<>();
	
	
	public static void exec(Runnable task) {
		
		execServ.execute(task);
	}

	public static void execFixedRate(Runnable command, long initialDelay,
            long period,
            TimeUnit unit){
		scheduleServ.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	
	public static void execFixedDelay(Runnable command,
            long initialDelay,
            long delay,
            TimeUnit unit){
		scheduleServ.scheduleWithFixedDelay(command,
                                                     initialDelay,
                                                     delay,
                                                     unit);
	}
	
	public static void addTask(ITask... tasks){
		if(tasks != null){
			for(ITask task : tasks){
				String taskName = task.name();
				if(task.interval() <= 0){
					LogUtils.warn("the interval of task '%s' is %d and it will be ignore to add to schedule.", taskName, task.interval());
					continue;
				}
				if(taskMap.containsKey(taskName)){
					throw new BaseRuntimeException("DATA_EXIST", "task name '" + taskName + "' already exists.");
				}
				
				
				Object firstDelay = task.firstExecDelay();
				if(firstDelay instanceof Long){
					LogUtils.debug("add task '%s' to execFixedDelay with firstDelay %d seconds and interval %d seconds.", taskName, (Long)firstDelay, task.interval());
					execFixedDelay(new TaskRunner(task), (Long)firstDelay, task.interval(), TimeUnit.SECONDS);
				} else if (firstDelay instanceof String){
					Date now = new Date();
					
					String dateTime = DateUtil.getDateStr(now) + " " + firstDelay;
					Date timeD = DateUtil.parseDateTime(dateTime);
					long diffTime = timeD.getTime() - now.getTime();
					diffTime = (diffTime > 0 ? diffTime : 24l * 3600 * 1000 + diffTime) ;
					long interval = task.interval() == 0 ? 24l * 3600 : task.interval();
					
					LogUtils.debug("add task '%s' to execFixedRate with firstDelay %d seconds(%s) and interval %d seconds.", taskName, diffTime, DateUtil.getDateTimeStr(now.getTime() + diffTime), task.interval());
					execFixedRate(new TaskRunner(task), diffTime / 1000, interval, TimeUnit.SECONDS);
				} else {
					throw new BaseRuntimeException("ILLEGAL_PARAM", "the defined return value for method firstExecDelay() is invalid in task '"+task.name()+"', only Long or Date can be acceptable.");
				}
				taskMap.put(taskName, task);
			}
		}
	}
	
	private static class TaskRunner implements Runnable{

		private ITask task;
		
		public TaskRunner(ITask task){
			this.task = task;
		}
		
		@Override
		public void run() {
			if(task != null){
				try{
					ThreadUtil.getPrefixMsgId("Task");
					LogUtils.info("begin to exec task '%s'...", task.name());
					long startTime = System.currentTimeMillis();
					
					task.execute();
					
					LogUtils.logXN("exec task " + task.name(), startTime);
				}catch(Throwable th){
					LogUtils.error("exec task '%s' error", th, task.name());
				}finally {
					ThreadUtil.clear();
				}
//				LogUtils.info("exec task '%s' over in %d milli seconds!", task.name(), (System.currentTimeMillis() - startTime));
			}
		}
		
	}
	
}
