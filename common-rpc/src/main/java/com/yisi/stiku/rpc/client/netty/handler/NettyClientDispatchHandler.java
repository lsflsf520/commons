package com.yisi.stiku.rpc.client.netty.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;

@Component
@Sharable
public class NettyClientDispatchHandler extends
		SimpleChannelInboundHandler<RpcResponse> {

	private final static Logger LOG = LoggerFactory
			.getLogger(NettyClientDispatchHandler.class);

	private final ConcurrentHashMap<String, BlockingQueue<RpcResponse>> responseMap = new ConcurrentHashMap<String, BlockingQueue<RpcResponse>>();

	@Override
	public void write(final ChannelHandlerContext ctx, final Object msg,
			final ChannelPromise promise) throws Exception {

		if (msg instanceof RpcRequest) {
			RpcRequest request = (RpcRequest) msg;
			responseMap.putIfAbsent(request.getMessageId(),
					new LinkedBlockingQueue<RpcResponse>(1));
		}
		super.write(ctx, msg, promise);
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx,
			final RpcResponse response) throws Exception {

		BlockingQueue<RpcResponse> queue = responseMap.get(response
				.getMessageId());
		// TODO　在并发量大的时候，这里有时候同一个消息会有重复接收的情况，不知道什么原因
		if (queue == null || !queue.isEmpty()) {
			LOG.warn("received duplicate message for messageId " + response.getMessageId() + ",discard it.");
		} else {
			LOG.debug("received response for request id " + response.getMessageId());
			queue.add(response);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {

		LOG.error("caught exception " + cause, cause);
		Enumeration<String> keys = responseMap.keys();
		while (keys != null && keys.hasMoreElements()) {
			String msgId = keys.nextElement();
			BlockingQueue<RpcResponse> queue = responseMap.get(msgId);
			if (queue != null && queue.size() <= 0) {
				queue.add(new RpcResponse(msgId, cause)); // 由于io过程中发生的异常，程序无法知道这个异常与哪个messageId对应，所以会通知当前所有处于等待状态的messageId发生了这个异常（这个地方不太合理，后期再改进）
			}
		}
		if (cause instanceof IOException) { // 这个地方如果抛出IOException异常，则将导致链接关闭
			ctx.close();
		}
	}

	public RpcResponse getResponse(final String messageId) throws IOException {

		RpcResponse result;
		responseMap.putIfAbsent(messageId,
				new LinkedBlockingQueue<RpcResponse>(1));
		try {
			int maxWaitTime = Integer.valueOf(ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "client.response.max.waitTime",
					"60"));
			result = responseMap.get(messageId).poll(maxWaitTime,
					TimeUnit.SECONDS);
			if (null == result) {
				throw new BaseRuntimeException("NO_MESSAGE_RECEIVED", "not received any message in " + maxWaitTime
						+ " seconds for messageId " + messageId);
			} else if (result.isError() && result.getException() instanceof IOException) {
				throw (IOException) result.getException();
			}
		} catch (final InterruptedException ex) {
			throw new IOException("等待服务器返回消息被中断, msgId:" + messageId, ex);
		} finally {
			responseMap.remove(messageId);
		}
		return result;
	}

	// private RpcResponse getSystemMessage(int seconds, long originMsgId) {
	// try {
	// BlockingQueue<RpcResponse> queue = responseMap
	// .get(Server.SYSTEM_MESSAGE_ID);
	// if (queue == null) {
	// throw new BaseRuntimeException("NO_MESSAGE",
	// "not received any message in " + seconds + " seconds for messageId " +
	// originMsgId);
	// }
	// return responseMap.get(Server.SYSTEM_MESSAGE_ID).poll(seconds,
	// SECONDS);
	// } catch (final InterruptedException ex) {
	// throw new BaseRuntimeException("SYS_ERROR",
	// "wait to get default response in " + seconds + " seconds for messageId "
	// + originMsgId);
	// }
	// }
}
