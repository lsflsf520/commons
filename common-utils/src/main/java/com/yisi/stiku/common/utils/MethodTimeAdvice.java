package com.yisi.stiku.common.utils;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class MethodTimeAdvice implements MethodInterceptor{

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		String className = invocation.getMethod().getDeclaringClass().getSimpleName();  
        //监控的方法名  
		long start = System.currentTimeMillis();
		Object result = null;  
        String methodName = className + "." + invocation.getMethod().getName();  
        try {  
            //这个是我们监控的bean的执行并返回结果  
            result = invocation.proceed();  
        } catch (Throwable e) {  
            System.out.println(methodName + " occur exception");
            throw e;  
        }  
        System.out.println("ms " + (System.currentTimeMillis() - start) + " method " + methodName );
        return result;
	}

}
