package com.yisi.stiku.log;

import com.yisi.stiku.common.utils.ThreadUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MsgIdConverter extends ClassicConverter{

	@Override
	public String convert(ILoggingEvent event) {
		return ThreadUtil.getTraceMsgId();
	}

}
