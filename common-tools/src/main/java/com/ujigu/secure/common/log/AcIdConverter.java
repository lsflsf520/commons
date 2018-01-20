package com.ujigu.secure.common.log;

import com.ujigu.secure.common.utils.ThreadUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 
 * @author liushangfeng
 *
 */
public class AcIdConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		return ThreadUtil.getAcId() + "";
	}

}
