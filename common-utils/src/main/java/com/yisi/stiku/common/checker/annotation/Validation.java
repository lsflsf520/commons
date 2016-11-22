package com.yisi.stiku.common.checker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yisi.stiku.common.checker.constant.RequiredType;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Validation {

	RequiredType required() default RequiredType.NONE;
	
	boolean email() default false;
	
	boolean mobile() default false;
	
	boolean digit() default false;
	
	boolean integer() default false;
	
	boolean unsignedInt() default false;
	
	/**
	 * 
	 * @return 用开闭区间的字符串格式表示
	 */
	String length() default ""; 
	
	String emailErrorMsg() default "格式不正确";
	String mobileErrorMsg() default "格式不正确";
	String digitErrorMsg() default "格式不正确";
	String integerErrorMsg() default "格式不正确";
	String unsignedIntErrorMsg() default "格式不正确";
	String lengthErrorMsg() default "长度在{0}到{1}之间";
	
	String fieldCnName() default "";
}
