package com.yisi.stiku.rpc.bean;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;


public abstract class BaseObject {
	
	private Collection<String> excludeFields = new HashSet<String>();
	
	public BaseObject() {
		this("excludeFields");
	}
	
	public BaseObject(final String... excludeFields) {
		this.excludeFields.addAll(Arrays.asList(excludeFields));
		this.excludeFields.add("excludeFields");
	}
	
	@Override
	public final int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, excludeFields);
	}
	
	@Override
	public final boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, excludeFields);
	}
	
	@Override
	public final String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, excludeFields);
	}
}
