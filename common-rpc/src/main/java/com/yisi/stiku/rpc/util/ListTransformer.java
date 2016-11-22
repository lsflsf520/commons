package com.yisi.stiku.rpc.util;

import java.util.List;

import org.apache.commons.lang.ClassUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public final class ListTransformer {
	
	private ListTransformer() { }
	
	public static List<String> encodingFromIso8859ToUtf8(final List<String> iso8859List) {
		return Lists.transform(iso8859List, new Function<String, String>() {
			
			@Override
			public String apply(final String iso8859Str) {
				return Encoding.encodingIso8859ToUtf8(iso8859Str);
			}
			
		});
	}
	
	public static <T extends Enum<T>> List<T> transformStringToEnum(final List<String> list, final Class<T> enumType) {
		return Lists.transform(list, new Function<String, T>() {
			
			@Override
			public T apply(final String enumName) {
				return Enum.valueOf(enumType, enumName);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static List<Class<?>> transformStringToClass(final List<String> list) {
		List<Class<?>> result = ClassUtils.convertClassNamesToClasses(list);
		if (result.contains(null)) {
			throw new IllegalArgumentException(String.format("Cannot convert '%s' to a class.", list.get(result.indexOf(null))));
		}
		return result;
	}
}
