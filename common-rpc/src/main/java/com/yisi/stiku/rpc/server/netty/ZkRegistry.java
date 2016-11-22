package com.yisi.stiku.rpc.server.netty;

import java.util.Set;

import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zkclient.AbstractListener;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.util.ZkRpcNodeInfoUtil;

public class ZkRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(ZkRegistry.class);
	
	
	public ZkRegistry() {
	}
	
	public void register(final RpcNode rpcNode, final Set<String> services){
		if(rpcNode != null){
			String nodeStr = ZkConstant.ALIAS_PROJECT_NAME + "@" + rpcNode.getHost() + ":" + rpcNode.getPort();
            final String content = ZkRpcNodeInfoUtil.buildNodeProp(rpcNode, services);
            
            String modulePath = ZkRpcNodeInfoUtil.buildNodePath(rpcNode.getGroup(), rpcNode.getModule());
            final String baseNodePath = modulePath + "/" + nodeStr;
            
            ConfigOnZk.createEphemeralNode(baseNodePath, content);
            
            ConfigOnZk.subscribeStateChanges(new AbstractListener() {
            	
            	@Override
            	public void handleStateChanged(KeeperState state)
            			throws Exception {
            		LOG.warn("zk state changed to " + state);
            	}
            	
            	@Override
            	public void handleNewSession() throws Exception {
            		LOG.debug("recreate rpc node " + baseNodePath);
            		ConfigOnZk.createEphemeralNode(baseNodePath, content);
            	}
            	
			});
            
            LOG.debug("rpc node {} has been created on zk", baseNodePath);
		}
	}
	
}
