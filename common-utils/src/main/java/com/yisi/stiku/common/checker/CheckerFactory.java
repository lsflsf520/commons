package com.yisi.stiku.common.checker;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.yisi.stiku.common.bean.BaseEntity;
import com.yisi.stiku.common.checker.annotation.Validation;
import com.yisi.stiku.common.checker.constant.OperType;
import com.yisi.stiku.common.checker.constant.RequiredType;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.StringUtil;

@Component
public class CheckerFactory implements ApplicationContextAware {

	private final static Logger LOG = LoggerFactory
			.getLogger(CheckerFactory.class);

	private final static Map<String, Checker> checkerMap = new HashMap<String, Checker>();

	private final static Set<String> excludeMethods = new HashSet<String>();
	
	static {
		excludeMethods.add("toString");
		excludeMethods.add("equals");
		excludeMethods.add("hashCode");
		excludeMethods.add("annotationType");
		excludeMethods.add("required");
		excludeMethods.add("fieldCnName");
		excludeMethods.add("errorSuffixMsg");
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		checkerMap.putAll(context.getBeansOfType(Checker.class));
	}

	public void checkBean(BaseEntity<?> entity, OperType operType) {
		Method[] methods = entity.getClass().getMethods();
		if (methods == null || methods.length <= 0) {
			return;
		}

		Map<String, Serializable> fieldKVMap = new HashMap<String, Serializable>();
		Map<String, Validation> fieldCheckerMap = new HashMap<String, Validation>();
		for (Method method : methods) {
			if (method.getName().startsWith("get")
//					&& (method.getParameters() == null || method.getParameters().length == 0)
					) {
				Validation validation = method.getAnnotation(Validation.class);

				if (validation != null) {
					Serializable value = invoke(method, entity);

					String fieldName = StringUtil.lowerFirst(method.getName()
							.replaceFirst("get", ""));

					fieldKVMap.put(fieldName, value);
					fieldCheckerMap.put(fieldName, validation);
				}
			}
		}

		if (fieldCheckerMap != null && !fieldCheckerMap.isEmpty()) {
			Set<String> fields = fieldCheckerMap.keySet();
			for (String field : fields) {
				Validation validation = fieldCheckerMap.get(field);
				RequiredType requiredType = validation.required();
				Serializable value = fieldKVMap.get(field);
				if (isNull(value)
						&& !RequiredType.NONE.equals(requiredType)
								&& (RequiredType.ALL.equals(requiredType)
								|| (RequiredType.INSERT.equals(requiredType) && RequiredType.INSERT
										.name().equals(operType.name())) || (RequiredType.UPDATE
								.equals(requiredType) && RequiredType.UPDATE
								.name().equals(operType.name())))) {
					throw new BaseRuntimeException("CHECK_ERROR",  (StringUtils.isBlank(validation.fieldCnName()) ? "字段" + field : validation.fieldCnName())
							+ "不能为空");
				}

				if (!isNull(value)) {
					Method[] validateMethods = validation.getClass()
							.getDeclaredMethods();
					Map<String/* method name */, Serializable/* value */> validateMethodMap = new HashMap<String, Serializable>();
					for (Method method : validateMethods) {
						if(
//								(method.getP == null || method.getParameters().length == 0)  && 
								!excludeMethods.contains(method.getName())){
							Serializable validateValue = invoke(method, validation);
							
							validateMethodMap.put(method.getName(), validateValue);
						}
					}
					
					for(String validateName : validateMethodMap.keySet()){
						Checker checker = checkerMap.get(validateName + "Checker");
						if(checker != null){
							boolean canpass = true;
							String suffixErrorMsg = (String)validateMethodMap.get(validateName + "ErrorMsg");
							try{
								canpass = checker.checkValue(value, validateMethodMap.get(validateName), validation, fieldKVMap);
							}catch(BaseRuntimeException e){
								canpass = false;
								if(StringUtils.isNotBlank(e.getResultCode().getErrorMsg())){
									suffixErrorMsg = e.getResultCode().getErrorMsg();
								}
							}
							if(!canpass){
								throw new BaseRuntimeException("CHECK_ERROR", (StringUtils.isBlank(validation.fieldCnName()) ? "字段" + field : validation.fieldCnName()) + suffixErrorMsg);
							}
						}
					}
					
				}

			}
		}

	}

	@SuppressWarnings("all")
	private Serializable invoke(Method method, Object obj) {
		try {
			return (Serializable) method.invoke(obj, null);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new BaseRuntimeException("CHECK_ERROR", "验证失败",
					"check failure for method '" + method.getName() + "'");
		}
	}

	private boolean isNull(Serializable value) {
		return (value == null || StringUtils.isBlank(value.toString()));
	}
}
