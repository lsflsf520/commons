package com.yisi.stiku.log;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.conf.ZkConstant;

public class LogUtil {
	
	private final static Logger XN_LOGGER = LoggerFactory.getLogger("xnLogger");
	private final static Logger OPER_LOGGER = LoggerFactory.getLogger("operLogger");
	private final static Logger ERROR_REPORT_LOGGER = LoggerFactory.getLogger("errorReportLogger");
	
	private final static String FIELD_SPLITER = " ";
	
	public final static String HTTP_TYPE = "http";
	public final static String RPC_CLIENT_TYPE = "rpc-client";
	public final static String RPC_SERVICE_TYPE = "rpc-service";
	public final static String DAO_TYPE = "dao";
	public final static String NORMAL_TYPE = "normal";
	
	private final static String LOCAL_IP = IPUtil.getLocalIp();
	
	/**
	 * 
	 * @param errorMsg 记录错误报告
	 */
	public static void reportError(String msgId, String errorMsg){
		String msg = (StringUtils.isBlank(msgId) ? null : msgId) + 
				FIELD_SPLITER + ZkConstant.PROJECT_NAME + 
				FIELD_SPLITER + ThreadUtil.getUserId() + 
				FIELD_SPLITER + ThreadUtil.getUserName() + 
				FIELD_SPLITER + errorMsg;
		ERROR_REPORT_LOGGER.info(msg);
	}
	
	/**
	 * 用于update操作
	 * @param preValObj 操作之前的数据对象
	 * @param postValObj 操作之后的数据对象
	 */
	public static void operLog(Object preValObj, Object postValObj){
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		
		String methodName = stacks[2].getClassName() + "." + stacks[2].getMethodName();
		
		String entityClazzName = postValObj.getClass().getName();
		
		operLog(methodName, entityClazzName, new Gson().toJson(preValObj), new Gson().toJson(postValObj));
	}
	
	/**
	 * 一般用于删除操作
	 * @param entityClassName 操作的数据对象类型
	 * @param postValObj 操作之后的数据对象
	 */
	public static void operLog(String entityClassName, Object postValObj){
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
			
		String methodName = stacks[2].getClassName() + "." + stacks[2].getMethodName();
		
		operLog(methodName, entityClassName, null, new Gson().toJson(postValObj));	
	}
	
	/**
	 * 一般用户insert操作
	 * @param postValObj 操作之后的数据对象
	 */
	public static void operLog(Object postValObj){
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		
		String methodName = stacks[2].getClassName() + "." + stacks[2].getMethodName();
		
		String entityClassName = postValObj.getClass().getName();
		
		operLog(methodName, entityClassName, null, new Gson().toJson(postValObj));
	}
	
	/**
	 * 通用方法
	 * @param entityClassName 操作的数据对象类型
	 * @param preVal 操作之前的数据对象
	 * @param postVal 操作之后的数据对象
	 */
	public static void operLog(String entityClassName, String preVal, String postVal){
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		
		String methodName = stacks[2].getClassName() + "." + stacks[2].getMethodName();
		
		operLog(methodName, entityClassName, preVal, postVal);
	}
	
	/**
	 * 通用方法
	 * @param methodName 执行该操作的方法名
	 * @param entityClassName 操作的数据对象类型
	 * @param preVal 操作之前的数据对象
	 * @param postVal 操作之后的数据对象
	 */
	public static void operLog(String methodName, String entityClassName, String preVal, String postVal){
		String log = ThreadUtil.getSrcProject() + 
				FIELD_SPLITER + ThreadUtil.getClientProject() + 
				FIELD_SPLITER + ThreadUtil.getSrcIP() +
				FIELD_SPLITER + ThreadUtil.getClientIP() + 
				FIELD_SPLITER + ZkConstant.PROJECT_NAME +
				FIELD_SPLITER + LOCAL_IP + 
				FIELD_SPLITER + ThreadUtil.getUserId() + 
				FIELD_SPLITER + ThreadUtil.getUserName() +
				FIELD_SPLITER + methodName + 
				FIELD_SPLITER + entityClassName + 
				"(" + preVal + ")" +
				"(" + postVal + ")";
				
		OPER_LOGGER.info(log);
	}
	
	/**
	 * 将获取调用该方法的方法名作为操作方法名，如果该操作方法的类名中包含Dao的字样，将标记该操作的 DAO的操作类型，否则没普通类型 
	 * @param millis 消耗的毫秒数
	 */
	public static void xnLog(long millis){
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		
		String clazzName = stacks[2].getClassName();
		String methodName = clazzName + "." + stacks[2].getMethodName();
		
		String type = NORMAL_TYPE;
		if(clazzName.contains("Dao") || clazzName.contains("dao") || clazzName.contains("DAO")){
			type = DAO_TYPE;
		}
		
		xnLog(type, methodName, millis);
	}
	
	/**
	 * @param type 性能日志的类型，有http、rpc-client、rpc-service、dao、normal
	 * @param methodName 方法名，也可以是uri
	 * @param millis 消耗的毫秒数
	 */
    public static void xnLog(String type, String methodName, long millis){
		String log = ThreadUtil.getTraceMsgId() + 
				   FIELD_SPLITER + type +
				   FIELD_SPLITER + ZkConstant.PROJECT_NAME + 
				   FIELD_SPLITER + LOCAL_IP + 
				   FIELD_SPLITER + methodName + 
				   FIELD_SPLITER + millis;
		
		XN_LOGGER.info(log);
	}
    
    
    
}
