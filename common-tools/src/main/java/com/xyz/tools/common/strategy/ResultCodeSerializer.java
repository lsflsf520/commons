package com.xyz.tools.common.strategy;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xyz.tools.common.bean.GlobalResultCode;
import com.xyz.tools.common.bean.ResultCodeIntf;
import com.xyz.tools.common.bean.ServiceResultCode;

/**
 * @author shangfeng
 *
 */
public class ResultCodeSerializer extends com.fasterxml.jackson.databind.JsonSerializer<ResultCodeIntf> implements
		JsonSerializer<ResultCodeIntf>, JsonDeserializer<ResultCodeIntf> {

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

	@Override
	public ResultCodeIntf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		String code = json.getAsString();
		
		try{
			return GlobalResultCode.valueOf(code);
		} catch (Exception e){
		}
		return new ServiceResultCode(code, null);
	}

}
