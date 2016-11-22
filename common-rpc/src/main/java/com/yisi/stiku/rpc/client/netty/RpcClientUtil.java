package com.yisi.stiku.rpc.client.netty;

import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.log.LogUtil;
import com.yisi.stiku.rpc.bean.AsynRpcRequest;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;
import com.yisi.stiku.rpc.cluster.Router;

/**
 * 
 * @author shangfeng
 *
 */
public class RpcClientUtil {

	/**
	 * 默认调用版本号为1.0.0的RPC接口的无参方法
	 * 
	 * @param router
	 *            rpc接口路由，由spring配置文件进行配置
	 * @param className
	 * @param methodName
	 * @return
	 * @throws Throwable
	 */
	public static <T> T sendRequest(Router router, String className,
			String methodName) throws Throwable {
		return sendRequest(router, className, "1.0.0", methodName, null, null);
	}

	/**
	 * 调用指定版本的RPC接口的无参方法
	 * 
	 * @param router
	 *            rpc接口路由，由spring配置文件进行配置
	 * @param className
	 * @param version
	 *            版本号（与服务端版版号对应）
	 * @param methodName
	 * @return
	 * @throws Throwable
	 */
	public static <T> T sendRequest(Router router, String className,
			String version, String methodName) throws Throwable {
		return sendRequest(router, className, version, methodName, null, null);
	}

	/**
	 * 
	 * @param router
	 *            rpc接口路由，由spring配置文件进行配置
	 * @param className
	 * @param version
	 *            版本号（与服务端版版号对应）
	 * @param methodName
	 * @param args
	 *            rpc接口的参数值
	 * @param parameterTypes
	 *            rpc接口的参数类型
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static <T> T sendRequest(Router router, String className,
			String version, String methodName, Object[] args,
			Class<?>[] parameterTypes) throws Throwable {
		RpcRequest request = new RpcRequest(className, version, methodName,
				args); // 创建并初始化 RPC 请求
		request.setParameterTypes(parameterTypes);

		return (T) sendRequest(router, request);
	}

	/**
	 * 异步调用rpc方法
	 * 
	 * @param router
	 * @param className
	 * @param version
	 * @param methodName
	 * @param args
	 * @param parameterTypes
	 * @return
	 * @throws Throwable
	 */
	public static boolean sendAsyncRequest(Router router, AsynRpcRequest request)
			throws Throwable {
		/*
		 * RpcOCntext.setArras Rpc.setParwser()
		 */

		return (Boolean) sendRequest(router, request);
	}

	/**
	 * 异步调用rpc方法
	 * 
	 * @param router
	 * @param className
	 * @param version
	 * @param methodName
	 * @param args
	 * @param parameterTypes
	 * @return
	 * @throws Throwable
	 */
	public static Object sendAsyncRequest(Router router, RpcRequest request)
			throws Throwable {

		return sendRequest(router, request);
	}

	/**
	 * 
	 * @param router
	 *            rpc接口路由，由spring配置文件进行配置
	 * @param clazz
	 * @param version
	 * @param methodName
	 * @param args
	 *            rpc接口的参数值
	 * @param parameterTypes
	 *            rpc接口的参数类型
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static <T> T sendRequest(Router router, Class<?> clazz,
			String version, String methodName, Object[] args,
			Class<?>[] parameterTypes) throws Throwable {
		RpcRequest request = new RpcRequest(clazz, version, methodName,
				args); // 创建并初始化 RPC 请求
		request.setParameterTypes(parameterTypes);

		return (T) sendRequest(router, request);
	}

	private static Object sendRequest(Router router, RpcRequest request)
			throws Throwable {
		request.putAll(ThreadUtil.getThreadInfoMap());
		request.addExtraInfo(RpcConstants.REQUEST_PROJECT,
				ZkConstant.PROJECT_NAME);

		long start = System.currentTimeMillis();

		RpcResponse response = router.send(request);

		LogUtil.xnLog(LogUtil.RPC_CLIENT_TYPE, request.getApiClassName() + "."
				+ request.getMethod(), System.currentTimeMillis() - start);
		if (response.isError()) {
			throw response.getException();
		}
		// 如果是异步请求，则直接返回true
		if (request instanceof AsynRpcRequest) {
			return true;
		}
		return response.getReturnValue();
	}

}
