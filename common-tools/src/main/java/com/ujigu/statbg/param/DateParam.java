package com.ujigu.statbg.param;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.statbg.GlobalParam;


/**
 * 根据指定的时间和单位，生成距当前时间的日期，返回的日期格式为 yyyy-MM-dd
 * @author lsf
 *
 */
public class DateParam implements GlobalParam<String>{

	private int time;
	private TimeUnit timeUnit;
	private String pattern;
	
	@Override
	public String generateParam() {
		Date date = DateUtil.timeAdd(new Date(), time, timeUnit == null ? TimeUnit.DAYS : timeUnit);
		return DateUtil.formatDate(date, StringUtils.isBlank(pattern) ? DateUtil.FORMAT_DATE : pattern);
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
