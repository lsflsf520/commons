package com.xyz.tools.statbg.param;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.utils.DateUtil;
import com.xyz.tools.statbg.GlobalParam;

/**
 * @author shangfeng
 *
 */
public class MonthParam implements GlobalParam<String> {

	private int monthNum; // 距days天后的日期所在月份的1日的monthNum个月后的日期。本月1号的monthNum月后，如果为负数，则代表monthNum月前
	private int days; //距离今天的days天
	private String pattern = DateUtil.FORMAT_DATE;
	
	/*
	 * 举例：
	 *   假如今天是  2016-08-01，monthNum=-1，days=-1
	 *   那么将先根据  2016-08-01 减去 1天，得到 2016-07-31；
	 *   再用 2016-07-31 计算出当天所在月份的1日，即 2016-07-01；
	 *   最后 用 2016-07-01 减去 1月，得到 2016-06-01;
	 */

	@Override
	public String generateParam() {

		Date dt = DateUtil.timeAddByDays(new Date(), days);
		Date date = DateUtil.parseDate((DateUtil.getFirstDayOfMonth(dt)));

		date = DateUtil.timeAddByMonth(date, monthNum);

		return DateUtil.formatDate(date, pattern);
	}

	public int getMonthNum() {

		return monthNum;
	}

	public void setMonthNum(int monthNum) {

		this.monthNum = monthNum;
	}

	public String getPattern() {

		return pattern;
	}

	public void setPattern(String pattern) {

		if (StringUtils.isNotBlank(pattern)) {
			this.pattern = pattern;
		}
	}

	
	public int getDays() {
	
		return days;
	}

	
	public void setDays(int days) {
	
		this.days = days;
	}
	

}
