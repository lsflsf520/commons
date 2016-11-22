package com.yisi.stiku.conf.zk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.AbstractResource;

import com.yisi.stiku.conf.BaseConfig;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZKClient;
import com.yisi.stiku.conf.ZkConstant;

@SuppressWarnings("unused")
public class ZookeeperResource extends AbstractResource implements DisposableBean {
	 // 配置的zk根
	private final static String ZKCONFIGS_CHROOT = BaseConfig.getValue("zk.config.prefix", ZkConstant.ZK_ROOT_NODE);
	private static final String URL_HEADER = "zk://";
	static Logger LOG = LoggerFactory.getLogger(ZookeeperResource.class);

	public static enum ReloadContext {
		AUTO, HOLD
	};

	public static enum OnConnectionFailed {
		IGNORE, THROW_EXCEPTION
	};

	public static enum PingCmd {
		get, ls
	}

	private String zkNodePaths;
	public String getZkNodePaths() {
		return zkNodePaths;
	}

	public void setZkNodePaths(String zkNodePaths) {
		if(StringUtils.isBlank(zkNodePaths)){
			throw new IllegalArgumentException("property zkNodePaths cannot be null");
		}
        if (zkNodePaths.startsWith(ZKCONFIGS_CHROOT) || zkNodePaths.startsWith("/")){
            this.zkNodePaths = zkNodePaths.replaceAll("/+", "/");
        }else{
			this.zkNodePaths = (ZKCONFIGS_CHROOT + "/" + (zkNodePaths.startsWith(ZkConstant.ALIAS_PROJECT_NAME + "/") ? zkNodePaths : ZkConstant.ALIAS_PROJECT_NAME + "/" + zkNodePaths)).replaceAll("/+", "/");
		}
	}

	@Override
	public boolean exists() {
		return ConfigOnZk.exists(zkNodePaths);
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public URL getURL() throws IOException {
		return new URL(URL_HEADER + ConfigOnZk.getZkConnStr() + zkNodePaths);
	}

	@Override
	public String getFilename() throws IllegalStateException {
		return zkNodePaths;
	}

	@Override
	public String getDescription() {
		return "Zookeeper resouce at '" + URL_HEADER + ConfigOnZk.getZkConnStr() + "', zkNode:'" + zkNodePaths + "'";
	}

	@Override
	public InputStream getInputStream() throws IOException {
		LOG.debug("use zk node '" +  zkNodePaths + "' as jdbc config");
		return new ByteArrayInputStream(ConfigOnZk.getByteData(zkNodePaths));
	}


//	@Override
//	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
//		this.ctx = (AbstractApplicationContext) ctx;
//	}

	@Override
	public void destroy() throws Exception {

	}



}
