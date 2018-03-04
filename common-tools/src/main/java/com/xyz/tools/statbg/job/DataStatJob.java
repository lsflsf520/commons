package com.xyz.tools.statbg.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.statbg.util.BootUtil;

/**
 * 定时数据统计执行器
 * @author lsf
 *
 */
public class DataStatJob extends AbstractSimpleJob{

	@Override
	public void executeJob(ShardingContext context) {
		String param = context.getJobParameter();
		if(StringUtils.isBlank(param)){
			throw new BaseRuntimeException("ILLEGAL_CONFIG", "at least a stat config file should be defined in job-parameter for job name " + context.getJobName());
		}
		
		String[] parts = param.split("\\s+");
		List<String> params = new ArrayList<>();
		params.add("-configFile");
		params.addAll(Arrays.asList(parts));
		
		BootUtil.execStat(params.toArray(new String[0]));
		
	}

}
