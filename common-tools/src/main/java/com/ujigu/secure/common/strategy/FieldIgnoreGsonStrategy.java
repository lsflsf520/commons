package com.ujigu.secure.common.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class FieldIgnoreGsonStrategy implements ExclusionStrategy {

		@Override
		public boolean shouldSkipClass(Class<?> arg0) {
			return false;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes feildAttr) {
			JsonIgnore anno = feildAttr.getAnnotation(JsonIgnore.class);
			if (anno != null) {
				return true;
			}
			return false;
		}

	};