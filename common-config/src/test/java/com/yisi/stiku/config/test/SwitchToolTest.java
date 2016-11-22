package com.yisi.stiku.config.test;

import java.util.Date;

import com.yisi.stiku.onoff.SwitchTool;

/**
 * @author shangfeng
 *
 */
public class SwitchToolTest {

	public static void main(String[] args) {

		// SwitchTool.setOn("update_student_info", 12345);
		// SwitchTool.setTimeout("update_student_info", 12345,
		// DateUtil.timeAddByDays(new Date(), 1));
		boolean result = SwitchTool.isOff("update_student_info", 12345);

		System.out.println(result);

		System.out.println(new Date(1456058593056l));
	}

}
