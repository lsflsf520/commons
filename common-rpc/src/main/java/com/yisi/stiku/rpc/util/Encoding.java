package com.yisi.stiku.rpc.util;

import java.io.UnsupportedEncodingException;

import com.yisi.stiku.rpc.exception.InvalidEncodingException;

public final class Encoding {
	
	private Encoding() { }
	
	public static String encodingIso8859ToUtf8(final String str) {
		return encoding(str, "ISO-8859-1", "UTF-8");
	}
	
	public static String encoding(final String str, final String formEncoding, final String toEncoding) {
		String result;
		if (null == str) {
			return null;
		}
		try {
			result = new String(str.getBytes(formEncoding), toEncoding);
		} catch (final UnsupportedEncodingException e) {
			throw new InvalidEncodingException(formEncoding, toEncoding);
		}
		return result;
	}
}
