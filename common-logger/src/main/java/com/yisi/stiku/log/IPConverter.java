package com.yisi.stiku.log;

import com.yisi.stiku.common.utils.IPUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class IPConverter extends ClassicConverter{

	@Override
	public String convert(ILoggingEvent event) {
		return IPUtil.getLocalIp();
	}

}
