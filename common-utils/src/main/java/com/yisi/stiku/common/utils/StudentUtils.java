package com.yisi.stiku.common.utils;

import java.util.Calendar;

public class StudentUtils {

	public static final int SPLIT_MONTH = 7;
	public static final int SPLIT_DAY = 1;
	
	public static int getGrade(int gradeId) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		if (month >= SPLIT_MONTH && day >= SPLIT_DAY) {
		} else {
			year = year - 1;
		}
		int gradeYear = year;
		switch (gradeId) {
		case 2:
			gradeYear = year - 1;
			break;
		case 3:
			gradeYear = year - 2;
			break;
		default:
			break;
		}
		return gradeYear;
	}

	public static int getGradeId(int gradeYear) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		if (month >= SPLIT_MONTH && day >= SPLIT_DAY) {
			year += 1;
		}
		year -= gradeYear;
		return year;
	}

	public static String getGradeName(int gradeYear) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		if (month >= SPLIT_MONTH && day >= SPLIT_DAY) {
			year += 1;
		}
		year -= gradeYear;
		if (year == 1) {
			return "高一";
		}
		if (year == 2) {
			return "高二";
		}
		if (year == 3) {
			return "高三";
		}
		return "已毕业";
	}
	
}
