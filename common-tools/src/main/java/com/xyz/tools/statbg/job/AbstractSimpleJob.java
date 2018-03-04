package com.xyz.tools.statbg.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.ThreadUtil;

abstract public class AbstractSimpleJob implements SimpleJob{

	@Override
	public void execute(ShardingContext context) {
		ThreadUtil.clear(); //先清除上一次线程执行留下的环境变量的干扰
		ThreadUtil.getPrefixMsgId("Job");
		LogUtils.info("begin exec job:%s, taskId:%s, param:%s, shardingItem:%s, shardingParam:%s, shardNum:%s", context.getJobName(), context.getTaskId(), context.getJobParameter(), context.getShardingItem(), context.getShardingParameter(), context.getShardingTotalCount());
		long startTime = System.currentTimeMillis();
		try{
			executeJob(context);
			LogUtils.info("end job:%s, taskId:%s, usedTime:%d millis", context.getJobName(), context.getTaskId(), System.currentTimeMillis() - startTime);
		} catch (Throwable e) {
			LogUtils.error("error exec job:%s, taskId:%s", e, context.getJobName(), context.getTaskId());
		}
	}
	
	abstract public void executeJob(ShardingContext context) throws Throwable;
	
}
