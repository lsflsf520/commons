package com.xyz.tools.common.utils;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.xyz.tools.common.bean.Pair;
import com.xyz.tools.common.bean.ResultCodeIntf;
import com.xyz.tools.common.strategy.FieldIgnoreGsonStrategy;
import com.xyz.tools.common.strategy.ResultCodeSerializer;

public class JsonUtil {

	private static final GsonBuilder INSTANCE = new GsonBuilder();

	static {
		/*INSTANCE.disableHtmlEscaping();
		INSTANCE.setDateFormat("yyyy-MM-dd HH:mm:ss");
		INSTANCE.setExclusionStrategies(new FieldIgnoreGsonStrategy());
		INSTANCE.registerTypeHierarchyAdapter(ResultCodeIntf.class, new ResultCodeSerializer());
		INSTANCE.registerTypeAdapter(PageBounds.class, new PageBoundsGsonAdapter());*/
//		INSTANCE.registerTypeAdapter(new TypeToken<Map<String, Object>>() {}.getType(), new MapTypeAdapter());
//		INSTANCE.registerTypeAdapter(Date.class, new DateGsonAdapter());
		
		init(INSTANCE);
	}
	
	private static void init(GsonBuilder builder){
		builder.disableHtmlEscaping();
		builder.setDateFormat("yyyy-MM-dd HH:mm:ss");
		builder.setExclusionStrategies(new FieldIgnoreGsonStrategy());
		builder.registerTypeHierarchyAdapter(ResultCodeIntf.class, new ResultCodeSerializer());
		builder.registerTypeAdapter(PageBounds.class, new PageBoundsGsonAdapter());
	}

	private JsonUtil(){}
	
	public static void setDateFormat(String pattern){
		if(StringUtils.isNotBlank(pattern)){
			INSTANCE.setDateFormat(pattern);
		}
	}
	
	public static Gson createTmpGson(Type type, Object typeAdapter){
		 GsonBuilder tmpBuilder = new GsonBuilder();
		 init(tmpBuilder);
		 tmpBuilder.registerTypeAdapter(type, typeAdapter);
		 
		 return tmpBuilder.create();
	}
	
	@SuppressWarnings("unchecked")
	public static Gson createTmpGson(List<Pair<Type, Object>> type2Adapters){
		 GsonBuilder tmpBuilder = new GsonBuilder();
		 init(tmpBuilder);
		 if(type2Adapters != null && type2Adapters.size() > 0){
			 for(Pair<Type, Object> pair : type2Adapters){
				 tmpBuilder.registerTypeAdapter(pair.first, pair.second);
			 }
		 }
		 
		 return tmpBuilder.create();
	}
	
	public static Gson create() {
		return INSTANCE.create();
	}

	public static class MapTypeAdapter extends TypeAdapter<Object> {

		@Override
		public Object read(JsonReader in) throws IOException {
			JsonToken token = in.peek();
			switch (token) {
			case BEGIN_ARRAY:
				List<Object> list = new ArrayList<Object>();
				in.beginArray();
				while (in.hasNext()) {
					list.add(read(in));
				}
				in.endArray();
				return list;

			case BEGIN_OBJECT:
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				in.beginObject();
				while (in.hasNext()) {
					map.put(in.nextName(), read(in));
				}
				in.endObject();
				return map;

			case STRING:
				return in.nextString();

			case NUMBER:
				/**
				 * 改写数字的处理逻辑，将数字值分为整型与浮点型。
				 */
				double dbNum = in.nextDouble();

				// 数字超过long的最大值，返回浮点类型
				if (dbNum > Long.MAX_VALUE) {
					return dbNum;
				}

				// 判断数字是否为整数值
				long lngNum = (long) dbNum;
				if (dbNum == lngNum) {
					return lngNum;
				} else {
					return dbNum;
				}

			case BOOLEAN:
				return in.nextBoolean();

			case NULL:
				in.nextNull();
				return null;

			default:
				throw new IllegalStateException();
			}
		}

		@Override
		public void write(JsonWriter out, Object value) throws IOException {
			
		}

	}
	
	public static class PageBoundsGsonAdapter implements JsonSerializer<PageBounds>, JsonDeserializer<PageBounds>{

		@Override
		public PageBounds deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject jsonObj = json.getAsJsonObject();
			JsonElement pageElem = jsonObj.get("page");
			
			int page = 1;
			int limit = 10;
			if(pageElem == null){
				pageElem = jsonObj.get("pageIndex");
			}
			if(pageElem != null){
				page = pageElem.getAsInt();
			}
			
			JsonElement limitElem = jsonObj.get("limit");
			if(limitElem == null){
				limitElem = jsonObj.get("pageSize");
			}
			if(limitElem != null){
				limit = limitElem.getAsInt();
			}
			
			return new PageBounds(page, limit);
		}

		@Override
		public JsonElement serialize(PageBounds src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("page", src.getPage());
			jsonObj.addProperty("limit", src.getLimit());
			
			return jsonObj;
		}
		
	}
	
	/*private static class DateGsonAdapter implements JsonSerializer<Date>, JsonDeserializer<Date>{

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return DateUtil.parseDateTime(json.getAsString());
		}

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(DateUtil.getDateTimeStr(src));
		}
		
	}*/
}
