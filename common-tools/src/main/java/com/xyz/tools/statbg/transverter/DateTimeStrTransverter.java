package com.xyz.tools.statbg.transverter;

import java.util.Date;

import com.xyz.tools.common.utils.DateUtil;



/**
 * 
 * @author lsf
 *
 */
public class DateTimeStrTransverter extends DateStrTransverter {

	@Override
	protected Date parseDate(String dateStr) {
		return DateUtil.parseDateTime(dateStr);
	}
	
}
