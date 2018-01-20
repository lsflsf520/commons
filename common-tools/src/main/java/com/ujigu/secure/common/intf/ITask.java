package com.ujigu.secure.common.intf;

/**
 * 实现本接口后，将启动定时任务刷新其缓存数据
 * @author lsf
 *
 */
public interface ITask {
	
	/**
	 * 启动缓存刷新的时间间隔，以秒为单位。
	 * 如果在firstExecDelay的返回值为Date的情况下，此方法返回0，则默认24小时执行一次定时任务
	 * @return
	 */
	long interval(); 
	
	/**
	 * 第一次执行execute方法的延迟时间，可以返回Long或者String
	 * 如果返回Long，则在指定的时间后第一次执行
	 * 如果返回String，格式必须为 HH:mm:ss, 即最近一天的HH:mm:ss 时间点开始执行第一次任务，否认则将抛出异常。注意，
	 * @return
	 */
	Object firstExecDelay();
	
	/**
	 * 指定缓存刷新
	 */
	void execute();
	
	/**
	 * 任务名称,便于管理
	 * @return
	 */
	String name();

}
