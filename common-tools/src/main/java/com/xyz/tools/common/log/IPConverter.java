package com.xyz.tools.common.log;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.common.utils.ThreadUtil;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 
 * @author liushangfeng
 *
 */
public class IPConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		String srcIP = ThreadUtil.getSrcIP();
		return StringUtils.isBlank(srcIP) ? IPUtil.getLocalIp() : srcIP;
	}

}
