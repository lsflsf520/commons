package com.xyz.tools.common.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalcMethodDuration {
	protected static final Logger log = LoggerFactory
			.getLogger(CalcMethodDuration.class);
	
	public void calcDuration(ProceedingJoinPoint joinpoint) throws Throwable{
		long start = System.currentTimeMillis();
		MethodSignature signature = (MethodSignature) joinpoint.getSignature();
		String methodName = signature.getMethod().getName();
		try {
			joinpoint.proceed();
		} catch (Throwable e) {
			log.debug("~~~~~~~~~~~~~~~Method ["+methodName+"] throw exception :"
					+(System.currentTimeMillis()-start)+"ms");
			throw e;
		}
		log.debug("~~~~~~~~~~~~~~~Method ["+methodName+"] :"+(System.currentTimeMillis()-start)+"ms");
	}
}
