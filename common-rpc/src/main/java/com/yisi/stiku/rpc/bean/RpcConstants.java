package com.yisi.stiku.rpc.bean;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.conf.BaseConfig;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;

public final class RpcConstants {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public final static int ZK_SESSION_TIMEOUT = 3000;

	public final static String ZK_RPC_ROOT_PATH = "/csjy/rpc";
	public final static String ZK_NODE_PATH = ZK_RPC_ROOT_PATH + "/nodes";

	public final static String DEFAULT_GRP = "defaultgrp";
	public final static String DEFAULT_MODULE = "defaultmod";

	public final static int DEFAULT_SERVER_PORT = 9097;// RPC 服务默认的端口号

	public final static int DEFAULT_HEARTBEAT_SECONDS = 30; // 默认的心跳间隔，以秒为单位

	public final static String RPC_CONFIG_NODE_PATH = ZkConstant.ALIAS_PROJECT_NAME + "/rpc_node.properties";

	public final static String KRYO_SERIALIZER_CONFIG_ZK_NODE = "common/kryo_serializer.properties";
	// public final static int DEFAULT_IDLE_TIME = 5; //默认5秒钟的闲置时间

	// public final static String CURR_USER_ID = "cui";
	// public final static String CURR_USER_NAME = "cun";
	// public final static String TRACE_MSG_ID = "tmi";

	public final static String REQUEST_PROJECT = "rp";

	private RpcConstants() {

	}

	public final static String getRpcHost() {

		String bindHost = BaseConfig.getValue("rpc.node.host");
		if (StringUtils.isBlank(bindHost)) {
			bindHost = ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "rpc.node.host", IPUtil.getLocalIp());
		}
		return bindHost;
	}

	public final static int getRpcPort() {

		return Integer.valueOf(ConfigOnZk.getValue(RpcConstants.RPC_CONFIG_NODE_PATH, "rpc.node.port",
				RpcConstants.DEFAULT_SERVER_PORT + ""));
	}
}
