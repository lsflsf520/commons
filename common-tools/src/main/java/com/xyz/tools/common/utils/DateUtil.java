package com.xyz.tools.common.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.exception.BaseRuntimeException;

/**
 * 日期转换工具
 */
public class DateUtil {

	private final static Logger LOG = LoggerFactory.getLogger(DateUtil.class);

	public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DATE = "yyyy-MM-dd";
	public static final String FORMAT_MONTH = "yyyy-MM";

	public static long SECOND_MILLIS = 1000L;
	public static long MINUTE_MILLIS = 60 * SECOND_MILLIS;
	public static long HOUR_MILLIS = 60 * MINUTE_MILLIS;
	public static long DAY_MILLIS = 24 * HOUR_MILLIS;
	public static long WEEK_MILLIS = 7 * DAY_MILLIS;

	/**
	 * 格式化时间，转换成int类型形如 20140705
	 *
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static Integer formatDate2Int(Date date, String pattern) {

		try {
			String result = formatDate(date, pattern);
			return Integer.parseInt(result);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 用户自己定义日期和格式，进行格式化
	 *
	 * @param date
	 *            用户指定的日期
	 * @param pattern
	 *            用户指定的时间格式
	 * @return 返回指定的格式化后的时间字符串
	 */
	public static String formatDate(Date date, String pattern) {

		if (date == null || StringUtils.isEmpty(pattern)) {
			return null;
		}
		SimpleDateFormat datePattern = new SimpleDateFormat(pattern);

		return datePattern.format(date);
	}

	/**
	 * 对指定的日期，使用 yyyy-MM 形式进行格式化
	 *
	 * @param date
	 *            指定的日期
	 * @return 返回 yyyy-MM 格式的字符串
	 */
	public static String getMonthStr(Date date) {

		if (date == null) {
			return null;
		}
		return new SimpleDateFormat(FORMAT_MONTH).format(date);
	}

	/**
	 * 对指定的日期，使用 yyyy-MM-dd 形式进行格式化
	 *
	 * @param date
	 *            指定的日期
	 * @return 返回 yyyy-MM-dd 格式的字符串
	 */
	public static String getDateStr(Date date) {

		if (date == null) {
			return null;
		}
		return new SimpleDateFormat(FORMAT_DATE).format(date);
	}

	/**
	 * 对指定的毫秒数，使用 yyyy-MM-dd 形式进行格式化
	 *
	 * @param timeMillis
	 *            指定的毫秒数
	 * @return 返回 yyyy-MM-dd 格式的字符串
	 */
	public static String getDateStr(long timeMillis) {

		return getDateStr(new Date(timeMillis));
	}

	/**
	 * 对指定的日期，使用 yyyy-MM-dd HH:mm:ss 形式进行格式化
	 *
	 * @param date
	 *            指定的日期
	 * @return 返回 yyyy-MM-dd HH:mm:ss 格式的字符串
	 */
	public static String getDateTimeStr(Date date) {

		if (date == null) {
			return null;
		}
		return new SimpleDateFormat(FORMAT_DATETIME).format(date);
	}

	/**
	 * 对指定的毫秒数，使用 yyyy-MM-dd HH:mm:ss 形式进行格式化
	 *
	 * @param timeMillis
	 *            指定的毫秒数
	 * @return 返回 yyyy-MM-dd HH:mm:ss 格式的字符串
	 */
	public static String getDateTimeStr(long timeMillis) {

		return getDateTimeStr(new Date(timeMillis));
	}

	/**
	 * @return 返回当前时间的 yyyy-MM-dd 格式的字符串
	 */
	public static String getCurrentDateStr() {

		return getDateStr(new Date());
	}

	/**
	 * @return 返回当前时间的 yyyy-MM-dd HH:mm:ss 格式的字符串
	 */
	public static String getCurrentDateTimeStr() {

		return getDateTimeStr(new Date());
	}

	/**
	 * @return 返回当前时间的 yyyy-MM 格式的字符串
	 */
	public static String getCurrentMonthStr() {

		return getMonthStr(new Date());
	}

	/**
	 * 在指定的日期的基础上添加指定单位的数值，然后格式化成 yyyy-MM-dd HH:mm:ss 的字符串后返回
	 *
	 * @param date
	 *            指定的日期
	 * @param diffTime
	 *            指定的时间数值（如果需要减，则使用负数即可）
	 * @param unit
	 *            指定的时间单位
	 * @return 返回 yyyy-MM-dd HH:mm:ss 格式的字符串
	 */
	public static String timeAddToStr(Date date, long diffTime, TimeUnit unit) {

		if (date == null) {
			return null;
		}
		long resultTime = date.getTime() + unit.toMillis(diffTime);

		return getDateTimeStr(resultTime);
	}

	/**
	 * 在指定的日期的基础上添加指定单位的数值，并返回
	 *
	 * @param date
	 *            指定的日期
	 * @param diffTime
	 *            指定的时间数值，可以为负数
	 * @param unit
	 *            指定的时间单位
	 * @return 返回计算之后的日期
	 */
	public static Date timeAdd(Date date, long diffTime, TimeUnit unit) {

		if (date == null) {
			return null;
		}
		long resultTime = date.getTime() + unit.toMillis(diffTime);

		return new Date(resultTime);
	}

	/**
	 * 在指定的日期的基础上添加指定单位的数值，并返回
	 *
	 * @param dateStr
	 *            指定的日期
	 * @param diffTime
	 *            指定的时间数值，可以为负数
	 * @param unit
	 *            指定的时间单位
	 * @return 返回计算之后的日期
	 */
	public static Date timeAdd(String dateStr, long diffTime, TimeUnit unit) {

		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		Date date = parseDate(dateStr);
		long resultTime = date.getTime() + unit.toMillis(diffTime);

		return new Date(resultTime);
	}
	
	/**
	 * 在指定的日期上添加指定days天数，然后返回
	 *
	 * @param date
	 *            指定的日期
	 * @param days
	 *            需要添加的天数，可以为负数
	 * @return 在指定的日期上添加指定days天数，然后返回
	 */
	public static Date timeAddByDays(Date date, int days) {

		return timeAdd(date, days, TimeUnit.DAYS);
	}

	/**
	 * @param date
	 *            日期
	 * @param months
	 *            需要添加的月份数，可以为负数
	 * @return 在指定的日期上添加指定months个月，然后返回
	 */
	public static Date timeAddByMonth(Date date, int months) {

		if (date == null) {
			return null;
		}
		if (months == 0) {
			return date;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, months);

		return cal.getTime();
	}

	/**
	 * 返回指定日期所在月份的第一天的日期
	 *
	 * @param date
	 * @return 返回指定日期所在月份的第一天的日期
	 */
	public static String getFirstDayOfMonth(Date date) {

		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		return getDateStr(cal.getTime());
	}
	
	/**
	 * 获取指定日期所在月份的最后一天
	 * @param date
	 * @return
	 */
	public static String getLastDayOfMonth(Date date){
		if (date == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		Date firstDay = cal.getTime();
		Date nextMonthFirstDay = timeAddByMonth(firstDay, 1);
		
		Date lastDay = timeAddByDays(nextMonthFirstDay, -1);
		
		return getDateStr(lastDay);
	}

	/**
	 * @return 返回昨天的日期字符串，格式为 yyyy-MM-dd
	 */
	public static String getYestoday() {

		return timeAddToStr(new Date(), -1, TimeUnit.DAYS).split(" ")[0];
	}

	/**
	 * 按照 yyyy-MM-dd 的格式解析给定的日期字符串
	 *
	 * @param dateStr
	 *            给定的日期字符串
	 * @return 返回解析后的日期，如果解析失败，则返回null
	 */
	public static Date parseDate(String dateStr) {

		try {
			return new SimpleDateFormat(FORMAT_DATE).parse(dateStr);
		} catch (ParseException e) {
			LOG.error("parse '" + dateStr + "' error", e);
		}

		return null;
	}
	
	/**
	 * 按一定格式解析给定日期，返回日期类型
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static Date parseDate(String dateStr, String pattern) {

		try {
			return new SimpleDateFormat(pattern).parse(dateStr);
		} catch (ParseException e) {
			LOG.error("parse '" + dateStr + "' error", e);
		}

		return null;
	}

	/**
	 * 按照 yyyy-MM-dd HH:mm:ss 的格式解析给定的日期字符串
	 *
	 * @param dateTimeStr
	 *            给定的日期字符串
	 * @return 返回解析后的日期，如果解析失败，则返回null
	 */
	public static Date parseDateTime(String dateTimeStr) {

		try {
			return new SimpleDateFormat(FORMAT_DATETIME).parse(dateTimeStr);
		} catch (ParseException e) {
			LOG.error("parse '" + dateTimeStr + "' error", e);
		}

		return null;
	}

	/**
	 * 按照指定的format格式解析给定的日期字符串
	 *
	 * @param dateStr
	 *            给定的日期字符串
	 * @param format
	 *            指定的日期格式
	 * @return 将日期字符串解析成Date对象
	 */
	public static Date parseToDate(String dateStr, String format) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			LOG.error("parse '" + dateStr + "' with pattern '" + format + "' error", e);
		}
		return date;
	}

	/**
	 * 将给定的日期字符串按照 yyyy-MM-dd HH:mm:ss 格式解析成Timestamp对象
	 *
	 * @param dateTimeStr
	 *            给定的日期字符串
	 * @return 返回解析成功后的Timestamp对象
	 */
	public static Timestamp parseTimestamp(String dateTimeStr) {

		Date date = parseDateTime(dateTimeStr);

		return convert(date);
	}

	/**
	 * @return 返回当前时间的Timestamp对象
	 */
	public static Timestamp getCurrentTimestamp() {

		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * @param date
	 *            指定的Date对象
	 * @return 将指定的Date对象转换成Timestamp对象
	 */
	public static Timestamp convert(Date date) {

		if (date == null) {
			return null;
		}
		return new Timestamp(date.getTime());
	}

	/**
	 * @param timestamp
	 *            指定的Timestamp对象
	 * @return 将指定的Timestamp对象转换成Date对象
	 */
	public static Date convert(Timestamp timestamp) {

		if (timestamp == null) {
			return null;
		}
		return new Date(timestamp.getTime());
	}

	/**
	 * 对给定的两个日期进行比较，如果date1 比 date2 大，则返回1；如果相等，则返回0；否则返回-1
	 *
	 * @param date1
	 * @param date2
	 * @return 对给定的两个日期进行比较，如果date1 比 date2 大，则返回1；如果相等，则返回0；否则返回-1
	 */
	public static int compare(Date date1, Date date2) {

		if (date1 == null) {
			return -1;
		}
		if (date2 == null) {
			return 1;
		}
		long timeDiff = date1.getTime() - date2.getTime();

		return timeDiff == 0 ? 0 : (int) (timeDiff / Math.abs(timeDiff));
	}

	/**
	 * 返回两个date2-date1 相差天数
	 * @param date
	 * @return
	 */
	public static int differentDays(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		int day1 = cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);
		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if (year1 != year2) {
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
					timeDistance += 366;
				} else
				{
					timeDistance += 365;
				}
			}

			return timeDistance + (day2 - day1);
		} else
		{
			return day2 - day1;
		}
	}
	
	public static Date getWeekMondayDate(Date date) {

		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setTime(date);
		if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
			calendar.add(Calendar.DAY_OF_YEAR, -1);
		}
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return calendar.getTime();
	}

	public static Date getWeekSundayDate(Date date) {

		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.DAY_OF_YEAR, 6);
		return calendar.getTime();
	}

	public static String formatDuring(long mss) {

		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (mss % (1000 * 60)) / 1000;
		return days + " days " + hours + " hours " + minutes + " minutes "
				+ seconds + " seconds ";
	}

	/**
	 * 获得相对星期几的日期。如relative=0,compareDay=Calendar.SUNDAY,nowDate=new Date(),则获取当前星期天的日期。
	 * 
	 * @param relativeNum
	 * @param compareDay
	 * @param nowDate
	 * @return
	 */
	public static Date getRelativeWeekDate(int relativeNum, int compareDay, Date nowDate)
	{

		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		int nowDay = cal.get(Calendar.DAY_OF_WEEK);
		int caculateDay = nowDay == Calendar.SUNDAY ? 7 : nowDay - 1;
		int compareCaculateDay = compareDay == Calendar.SUNDAY ? 7 : compareDay - 1;
		int restDays = compareCaculateDay - caculateDay;
		int totalRestDays = 7 * relativeNum + restDays;
		cal.add(Calendar.DATE, totalRestDays);
		cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);		
		Date retDate = cal.getTime();
		return retDate;
	}
	
	
	/**
	 * 获取当前日期的23.59.59
	* @Title: getEndOfDate 
	* @Description: 获取当前日期的23.59.59
	* @param @param date
	* @param @return
	* @return Date 返回类型
	* @throws
	 */
	public static Date getEndOfDate(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date result = null;
		if(date != null){
			try {
				result = timeFormat.parse(dateFormat.format(date)+" 23:59:59");
			} catch (ParseException e) {
				result = null;
			}
		}
		return result;
	}
	
	/**
	 * 生成开始日期到结束日期之间的连续天数，如果endDate比当前
	 * @param startDate
	 * @param endDate
	 * @param ignoreAfterDays 是否忽略掉当前时间之后的天数
	 * @return
	 */
	public static List<String> genDays(Date startDate, Date endDate, boolean ignoreAfterDays){
		List<String> days = new ArrayList<>();
		Date now = new Date();
		if(ignoreAfterDays && endDate.getTime() > now.getTime()){
			endDate = now;
		}
		if(startDate.getTime() > endDate.getTime()){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "开始时间不能大于结束时间");
		}
		
		
		while(startDate.getTime() <= endDate.getTime()){
			String currDay = DateUtil.getDateStr(startDate);
			
			days.add(currDay);
			
			startDate = DateUtil.timeAddByDays(startDate, 1);
		}
		
		return days;
	}
	
	/**生成起始月到结束月的集合
	 * @param startMonth   yyyy-mm
	 * @param endMonth     yyyy-mm
	 * @return
	 */
	public static List<String> genMonths(Date startMonth, Date endMonth){
		List<String> months = new ArrayList<>();
		if(startMonth.getTime() > startMonth.getTime()){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "开始月份不能大于结束月份");
		}
		Calendar dd = Calendar.getInstance();//定义日期实例
		dd.setTime(startMonth);//设置日期起始时间

		while(!dd.getTime().after(endMonth)){//判断是否到结束日期
			months.add(formatDate(dd.getTime(),FORMAT_MONTH));
			dd.add(Calendar.MONTH, 1);//进行当前日期月份加1
		}
		return months;
	}
	
	/**
	 * @return
	 * @Decription 判断是否当月第一天
	 * @Author Administrator
	 * @Time 2017年8月21日下午4:21:59
	 * @Exception
	 */
	public static boolean isFirstDayOfMonth(){
		Calendar c = Calendar.getInstance();
		int today = c.get(c.DAY_OF_MONTH);
		if(today == 1){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @param birthday
	 * @return
	 * @Decription 通过生日计算年龄
	 * @Author Administrator
	 * @Time 2017年8月21日下午4:21:28
	 * @Exception
	 */
	public static int getAge(Date birthday){
		Calendar calendar = Calendar.getInstance();
		Date now = new Date();
		if(now.before(birthday)){
			throw new IllegalArgumentException("生日不能比当前时间晚！");
		}
		int yearNow = calendar.get(calendar.YEAR);
		int monthNow = calendar.get(calendar.MONTH)+1;
		int dayNow = calendar.get(calendar.DAY_OF_MONTH);
		calendar.setTime(birthday);
		int yearBirth = calendar.get(calendar.YEAR);
		int monthBirth = calendar.get(calendar.MONTH)+1;
		int dayBirth = calendar.get(calendar.DAY_OF_MONTH);
		int age = yearNow-yearBirth;
		if(monthNow <= monthBirth){
			if(monthNow == monthBirth){
				if(dayNow < dayBirth){
					age--;
				}
			}else{
				age--;
			}
		}
		return age;
	}
	
	/**
	 * @param cardId
	 * @return
	 * @Decription 通过身份证获取生日
	 * @Author Administrator
	 * @Time 2017年11月9日下午5:48:29
	 * @Exception
	 */
	public static Date getBirthdayByCardId(String cardId){
		String year = cardId.substring(6, 10);
		String month = cardId.substring(10, 12);
		String day = cardId.substring(12, 14);
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(year+"-"+month+"-"+day);
		} catch (ParseException e) {
			LogUtils.error("error get birthday fom id card , id : ", e, cardId);
			return null;
		}
	}
	
	/**
	 * @param birthday
	 * @return
	 * @throws ParseException 
	 * @Decription 根据生日获取还有多少天生日
	 * @Author Administrator
	 * @Time 2017年11月9日下午5:53:10
	 * @Exception
	 */
	public static int getBithdayNum(Date birthday) throws ParseException{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(birthday);
		calendar.add(Calendar.YEAR, getAge(birthday)+1);
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		today = sdf.parse(sdf.format(today));
		int days = (int) ((calendar.getTime().getTime() - today.getTime())/(1000*3600*24));
		return days;
	}
}
