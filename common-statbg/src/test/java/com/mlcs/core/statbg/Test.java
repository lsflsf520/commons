package com.mlcs.core.statbg;

import java.util.Date;

import com.yisi.stiku.common.utils.DateUtil;

public class Test {

	public static void main(String[] args) {

		// Person p = new Man();
		// System.out.println(p.getAge());
		//
		// Man m = new Man();
		// System.out.println(m.getAge());
		//
		// m.print();

		Date date = DateUtil.getWeekMondayDate(DateUtil.parseDate("2016-07-18"));
		System.out.println(DateUtil.getDateTimeStr(date));

	}

	static class Person {

		protected int age = 10;

		public int getAge() {

			return age;
		}
	}

	static class Man extends Person {

		protected int age = 30;

		public void print() {

			System.out.println(age);
		}

	}

}
