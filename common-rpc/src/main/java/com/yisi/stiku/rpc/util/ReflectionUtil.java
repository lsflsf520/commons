package com.yisi.stiku.rpc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.rpc.exception.ReflectionException;

public final class ReflectionUtil {
	
	private final static Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);
	
	private ReflectionUtil() { }
	
	public static <T> T newInstance(final Class<T> clazz) {
		T result = null;
		try {
			result = clazz.newInstance();
		} catch (InstantiationException e) {
			LOG.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage(), e);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Field> getAllNonStaticFields(final Class<?> clazz) {
		Collection<Field> result = new LinkedHashSet<Field>();
		result.addAll(getNonStaticFields(ClassUtils.getAllSuperclasses(clazz)));
		result.addAll(getNonStaticFields(clazz));
		return result;
	}
	
	private static Collection<Field> getNonStaticFields(final Collection<Class<?>> classes) {
		Collection<Field> result = new ArrayList<Field>();
		for (Class<?> each : classes) {
			result.addAll(getNonStaticFields(each));
		}
		return result;
	}
	
	private static Collection<Field> getNonStaticFields(final Class<?> clazz) {
		Collection<Field> result = new ArrayList<Field>();
		for (Field each : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(each.getModifiers())) {
				result.add(each);
			}
		}
		return result;
	}
	
	public static String[] getNullPropertyNames(final Object source) {
		Set<String> result = new HashSet<String>();
		for (Field each : getAllNonStaticFields(source.getClass())) {
			each.setAccessible(true);
			Object value = null;
			try {
				value = each.get(source);
			} catch (IllegalArgumentException e) {
				throw new ReflectionException(e.getMessage());
			} catch (IllegalAccessException e){
				throw new ReflectionException(e.getMessage());
			}
			if (null == value) {
				result.add(each.getName());
			}
		}
		return result.toArray(new String[result.size()]);
	}
}
