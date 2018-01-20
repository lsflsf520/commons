package com.ujigu.secure.web.convert;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;

import com.ujigu.secure.common.utils.DateUtil;

public class DateConvert implements Converter<String, Date>{

	@Override
	public Date convert(String val) {
		if(StringUtils.isBlank(val)){
			return null;
		}
		if(val.contains("\"")){
			val = val.replace("\"", "");
		}
		if(val.contains("'")){
			val = val.replace("'", "");
		}
		if(val.contains(":")){
			return DateUtil.parseDateTime(val);
		}
		
		return DateUtil.parseDate(val);
	}

}
