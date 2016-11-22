package com.yisi.stiku.statbg.func;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author shangfeng
 *
 */
@Service
public class URLDecoder implements Func<String> {

	private final static Logger LOG = LoggerFactory.getLogger(URLDecoder.class);

	@Override
	public String convert(Object value, Map<String, String> funcParamMap) {

		if (value instanceof String) {
			try {
				String enc = funcParamMap == null ? null : funcParamMap.get("0");

				return java.net.URLDecoder.decode(value.toString(), StringUtils.isNotBlank(enc) ? enc : "UTF-8");
			} catch (UnsupportedEncodingException e) {
				LOG.warn("decode value '" + value + "' error", e);
			}
		}

		return value == null ? null : value.toString();
	}

}
