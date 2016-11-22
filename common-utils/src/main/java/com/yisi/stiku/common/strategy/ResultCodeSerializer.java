package com.yisi.stiku.common.strategy;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.yisi.stiku.common.bean.ResultCodeIntf;

/**
 * @author shangfeng
 *
 */
public class ResultCodeSerializer extends com.fasterxml.jackson.databind.JsonSerializer<ResultCodeIntf> implements
		JsonSerializer<ResultCodeIntf> {

	@Override
	public JsonElement serialize(ResultCodeIntf src, Type typeOfSrc, JsonSerializationContext context) {

		return src == null ? null : new JsonPrimitive(src.getCode());
	}

	@Override
	public void serialize(ResultCodeIntf src, JsonGenerator gen, SerializerProvider serializers) throws IOException,
			JsonProcessingException {

		if (src == null) {
			gen.writeNull();
		} else {
			gen.writeString(src.getCode());
		}
	}

}
