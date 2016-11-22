package com.yisi.stiku.rpc.bean;

import org.apache.commons.lang.StringUtils;

public class AsynRpcRequest extends RpcRequest {

	private static final long serialVersionUID = 1407365868128066669L;
	protected String callBackInterface;
	protected String callBackMethod;
	protected Object[] callBackargs;
	protected Class<?>[] callBackargClasses;
	protected Throwable exception;
	protected Object result;
	protected boolean executeOnServer;

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	private AsynRpcRequest(final String apiClassName,
			final String serviceVersion, final String method, 
			final Object... parameters) {
		super(apiClassName, serviceVersion, method, parameters);
	}

	public String getCallBackInterface() {
		return callBackInterface;
	}

	public String getCallBackMethod() {
		return callBackMethod;
	}

	public Object[] getArgs() {
		return callBackargs;
	}

	public Class<?>[] getArgClasses() {
		return callBackargClasses;
	}

	public void setCallBackargs(Object[] callBackargs) {
		this.callBackargs = callBackargs;
	}

	public Object getResult() {
		return result;
	}

	public boolean isExecuteOnServer() {
		return executeOnServer;
	}

	public Throwable getThrowable() {
		return exception;
	}

	public static AsynRpcRequest newInstance(String className, String version,
			String methodName, Object[] args, Class<?>[] parameterTypes) {
		AsynRpcRequest asynRpcRequest = new AsynRpcRequest(className, version,
				methodName, args);
		asynRpcRequest.setParameterTypes(parameterTypes);
		return asynRpcRequest;

	}

	public AsynRpcRequest buildCallBackRequest(boolean executeOnServer,
			String callBackInterface, String callBackMethod,
			Object[] callBackargs, Class<?>[] callBackargClasses) {
		this.callBackInterface = callBackInterface;
		this.callBackMethod = callBackMethod;
		this.callBackargs = callBackargs;
		this.callBackargClasses = callBackargClasses;
		this.executeOnServer = executeOnServer;
		return this;

	}

	public RpcRequest genCallbackRequest() {
		//如果回调接口为空，则返回一个空的请求
		if(StringUtils.isBlank(this.callBackInterface)){
			return null;
		}
		RpcRequest rpcRequest = new RpcRequest(this.callBackInterface,
				this.serviceVersion, this.callBackMethod,
				this.callBackargs);
		rpcRequest.setParameterTypes(this.getArgClasses());

		return rpcRequest;

	}
}
