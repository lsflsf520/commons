package com.xyz.tools.db.aop;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.db.utils.MybatisUtil;

@Intercepts({@Signature(
		type= Executor.class,
		method = "query",
		args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class QueryInterceptor implements Interceptor {
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		
		long startTime = System.currentTimeMillis();
		Object retVal = invocation.proceed();
		final Object[] queryArgs = invocation.getArgs();
		final MappedStatement ms = (MappedStatement)queryArgs[0];
		
		String intfName = MybatisUtil.parseShortStmtName(ms.getId());
		LogUtils.logXN(intfName, startTime); //记录性能日志
        
		return retVal;
	}
	

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
