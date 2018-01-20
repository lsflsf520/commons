package com.ujigu.secure.common.checker.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.ujigu.secure.common.checker.Checker;
import com.ujigu.secure.common.checker.annotation.Validation;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.RegexUtil;

@Component
public class LengthChecker implements Checker {

	@Override
	public boolean checkValue(Serializable value, Serializable regular, Validation validation,
			Map<String, Serializable> checkFieldMap) {
		if(regular == null || StringUtils.isBlank(regular.toString())){
			return true;
		}
		int vallen = value.toString().length();
		String regularStr = regular.toString();
		if(RegexUtil.isUnsignedInt(regularStr)){
			int length = Integer.valueOf(regularStr);
			return vallen == length;
		}
		
		if(!RegexUtil.isOCExpression(regularStr)){
			throw new BaseRuntimeException("REGULAR_ERROR", "长度验证表达式格式错误");
		}
		
		String[] parts = regularStr.split(",");
		int minlen = 0;
		int maxlen = Integer.MAX_VALUE;
		if(parts[0].startsWith("[")){
			String minstr = parts[0].replace("[", "").trim();
			if(StringUtils.isNotBlank(minstr)){
				minlen = Integer.valueOf(minstr);
			}
		}else if(parts[0].startsWith("(")){
			String minstr = parts[0].replace("(", "").trim();
			if(StringUtils.isNotBlank(minstr)){
				minlen = Integer.valueOf(minstr) + 1;
			}
		}
		
		if(parts[1].endsWith("]")){
			String maxStr = parts[1].replace("]", "").trim();
			if(StringUtils.isNotBlank(maxStr)){
				maxlen = Integer.valueOf(maxStr);
			}
		}else if(parts[1].endsWith(")")){
			String maxStr = parts[1].replace(")", "").trim();
			if(StringUtils.isNotBlank(maxStr)){
				maxlen = Integer.valueOf(maxStr) - 1;
			}
		}
		
		boolean result = vallen >= minlen && vallen <= maxlen;
		
		if(!result){
			throw new BaseRuntimeException("CHECK_ERROR", MessageFormat.format(validation.lengthErrorMsg(), minlen, maxlen));
		}
		
		return result;
	}

}
