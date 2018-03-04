package com.xyz.tools.common.log;

import com.xyz.tools.common.utils.ThreadUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 
 * @author liushangfeng
 *
 */
public class TraceIdConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		return ThreadUtil.getTraceMsgId();
	}

}
