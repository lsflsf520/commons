package com.yisi.stiku.rpc.server.netty.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.log.LogUtil;
import com.yisi.stiku.rpc.bean.AsynRpcRequest;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;
import com.yisi.stiku.rpc.client.netty.RpcClientUtil;
import com.yisi.stiku.rpc.cluster.Router;

@Component
@Sharable
public class NettyServerDispatchHandler extends
		SimpleChannelInboundHandler<RpcRequest> implements
		ApplicationContextAware {

	private final static Logger LOG = LoggerFactory
			.getLogger(NettyServerDispatchHandler.class);

	private ApplicationContext applicationContext;

	private ExecutorService executor = null;

	private ExecutorService getExecutor() {

		if (executor == null) {
			synchronized (NettyServerDispatchHandler.class) {
				if (executor == null) {
					String bgThreads = ConfigOnZk
							.getValue(ZkConstant.APP_ZK_PATH,
									"bg.thread.pool.size", "20");
					executor = Executors.newFixedThreadPool(Integer
							.valueOf(bgThreads));
				}
			}
		}

		return executor;
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx,
			final RpcRequest request) {

		final String requestMethod = request.getApiClassName() + "."
				+ request.getMethod();
		LOG.debug("will exec " + requestMethod + " with request id "
				+ request.getMessageId());
		try {
			ThreadUtil.putAllThreadInfo(request.getExtraInfoMap());

			InetSocketAddress clientAddr = (InetSocketAddress) ctx.channel()
					.remoteAddress();
			ThreadUtil.setClientIP(clientAddr.getAddress().getHostAddress());
			ThreadUtil.setClientProject((String) request
					.getExtraInfo(RpcConstants.REQUEST_PROJECT));

			long start = System.currentTimeMillis();

			Object returnValue = null;
			if (request instanceof AsynRpcRequest) {
				asyncExecute(request, requestMethod);
			} else {
				returnValue = execute(request);
			}

			LogUtil.xnLog(LogUtil.RPC_SERVICE_TYPE, requestMethod,
					System.currentTimeMillis() - start);

			RpcResponse response = new RpcResponse(request.getMessageId(),
					returnValue);
			response.putAll(request.getExtraInfoMap());
			ctx.writeAndFlush(response);
			LOG.debug("write response for request id " + request.getMessageId());
		} catch (Throwable th) {
			Throwable ite = catchRealTh(th, request, requestMethod);
			ite.setStackTrace(ite.getStackTrace()); // 此处设置堆栈，是为了确保异常（譬如NullPointException有可能就没有为stackTrace字段赋值，导致客户端拿不到准确的堆栈信息）的
													// stackTrace 属性有值
			RpcResponse response = new RpcResponse(request.getMessageId(), ite);
			response.putAll(request.getExtraInfoMap());
			ctx.writeAndFlush(response);
			if (!(ite instanceof BaseRuntimeException)) {
				LOG.error("request " + request.getMessageId() + "," +
						"invoke "
						+ requestMethod + " error", ite);
			}
		}
	}

	private void asyncExecute(final RpcRequest request, final String requestMethod) {

		// 如果是异步请求，则将请求放入线程池中执行，同时 returnValue 会为null
		getExecutor().execute(new Runnable() {

			@Override
			public void run() {

				AsynRpcRequest asynsRpcRequest = (AsynRpcRequest) request;
				Object result = null;
				Throwable ex = null;
				try {
					result = execute(request);
				} catch (InvocationTargetException th) {
					ex = catchRealTh(th, request, requestMethod);

				} catch (Throwable e) {
					ex = e;
					LOG.error("request " + request.getMessageId() + ","
							+ "invoke " + requestMethod + " error", e);
				} finally {
					asynsRpcRequest.setException(ex);
					asynsRpcRequest.setResult(result);
					asynsRpcRequest
							.setCallBackargs(new Object[] { asynsRpcRequest });
				}

				RpcRequest callbackRequest = asynsRpcRequest
						.genCallbackRequest();
				if (callbackRequest == null) {
					LOG.warn("there is no callback service for {msgId:" + request.getMessageId() + ", methodName:"
							+ requestMethod + "}");
					return;
				}
				if (asynsRpcRequest.isExecuteOnServer()) {
					try {
						execute(callbackRequest);
					} catch (InvocationTargetException e) {
						LOG.error("服务端异步回调失败, {pre-msgId:" + request.getMessageId() + ", pre-methodName:" + requestMethod
								+ "}, {callback-msgId:" + callbackRequest.getMessageId() + ", callback-methodName:"
								+ callbackRequest.getApiClassName() + "." + callbackRequest.getMethod() + "}", e);
					}
				} else {
					try {
						Router router = applicationContext
								.getBean(Router.class);

						if (null == router) {
							LOG.error("异步回调无法找到Router");
						}
						RpcClientUtil.sendAsyncRequest(router,
								callbackRequest);
					} catch (Throwable e) {
						LOG.error("客户端异步回调失败, {pre-msgId:" + request.getMessageId() + ", pre-methodName:" + requestMethod
								+ "}, {callback-msgId:" + callbackRequest.getMessageId() + ", callback-methodName:"
								+ callbackRequest.getApiClassName() + "." + callbackRequest.getMethod() + "}", e);
					}
				}
			}

		});
	}

	private Throwable catchRealTh(Throwable th, final RpcRequest request, String requestMethod) {

		Throwable ite = th;
		if (th instanceof InvocationTargetException) {
			ite = ((InvocationTargetException) th).getTargetException();
			if (ite instanceof BaseRuntimeException) {
				BaseRuntimeException bre = (BaseRuntimeException) ite;
				if (bre.getCause() != null) {
					LOG.error("msgId:" + request.getMessageId() + ",method:" + requestMethod + ","
							+ bre.getMessage(), bre);
				} else {
					LOG.warn("msgId:" + request.getMessageId() + ",method:" + requestMethod + ","
							+ bre.getMessage());
				}
				ite = bre;
			}
		}

		return ite;
	}

	private Object execute(final RpcRequest request)
			throws InvocationTargetException {

		Object result = null;
		// try {
		Object apiInstance = applicationContext.getBean(request.getApiClass());

		// Method method = apiInstance.getClass().getMethod(request.getMethod(),
		// getParameterTypes(request.getParameters()));
		// result = method.invoke(apiInstance, request.getParameters());
		FastClass serviceFastClass = FastClass.create(request.getApiClass());
		FastMethod serviceFastMethod = serviceFastClass.getMethod(
				request.getMethod(), request.getParameterTypes());

		result = serviceFastMethod.invoke(apiInstance, request.getParameters());
		// } catch (SystemException ex){
		// throw new ServerException(request.getMessageId(), ex);
		// }
		return result;
	}

	// private Class<?>[] getParameterTypes(final Object[] parameters) {
	// Class<?>[] result = new Class<?>[parameters.length];
	// int i = 0;
	// for (Object each : parameters) {
	// result[i] = each.getClass();
	// i++;
	// }
	// return result;
	// }

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable cause) throws Exception {

		LOG.error(cause.getMessage(), cause);

		ctx.close(); // 发生网络异常，则关闭当前连接
	}

	@Override
	public void setApplicationContext(
			final ApplicationContext applicationContext) throws BeansException {

		this.applicationContext = applicationContext;
	}

}
