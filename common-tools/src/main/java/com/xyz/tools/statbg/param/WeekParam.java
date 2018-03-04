package com.xyz.tools.statbg.param;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.DateUtil;
import com.xyz.tools.statbg.GlobalParam;

/**
 * @author shangfeng
 *
 */
public class WeekParam implements GlobalParam<String> {

	private String weekDay = "M"; // M(星期一) or S(星期日)
	private int weekNum; // 距days后的周(一或日)的weekNum周后，如果为负数，则代表weekNum周前
	private int days; // 距离今天的days天
	private String pattern = DateUtil.FORMAT_DATE;

	@Override
	public String generateParam() {

		Date dt = DateUtil.timeAddByDays(new Date(), days);

		Date date = null;
		if ("M".equalsIgnoreCase(weekDay)) {
			date = DateUtil.getWeekMondayDate(dt);
		} else if ("S".equalsIgnoreCase(weekDay)) {
			date = DateUtil.getWeekSundayDate(dt);
		} else {
			throw new BaseRuntimeException("NOT_SUPPORT",
					"Only S or M support for class com.yisi.stiku.statbg.param.WeekParam");
		}

		date = DateUtil.timeAdd(date, weekNum * 7, TimeUnit.DAYS);

		return DateUtil.formatDate(date, pattern);
	}

	public String getWeekDay() {

		return weekDay;
	}

	public void setWeekDay(String weekDay) {

		this.weekDay = weekDay;
	}

	public int getWeekNum() {

		return weekNum;
	}

	public void setWeekNum(int weekNum) {

		this.weekNum = weekNum;
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
