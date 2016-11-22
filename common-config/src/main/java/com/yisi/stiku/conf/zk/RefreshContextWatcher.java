package com.yisi.stiku.conf.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.yisi.stiku.conf.ZKClient;
import com.yisi.stiku.conf.zk.ZookeeperResource.ReloadContext;

public class RefreshContextWatcher implements Watcher {

    private static Logger log = LoggerFactory.getLogger(ZKClient.class);

    private AbstractApplicationContext ctx;
    private boolean regressionZnodes;
    private ReloadContext reloadContext;

    public RefreshContextWatcher(AbstractApplicationContext ctx, boolean regressionZnodes, ReloadContext reloadContext) {
        this.ctx = ctx;
        this.reloadContext = reloadContext;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
        case NodeChildrenChanged:
            if (!regressionZnodes) {
                break;
            }
        case NodeDataChanged:
            log.info("Detected ZNode or sub ZNode changed.", null, null);
            switch (reloadContext) {
            case AUTO:
                log.info("Refresh spring context.", null, null);
                ctx.refresh();
                break;
            case HOLD:
                log.info("Keep context unchange according to configuration.", null, null);
                break;
            }
            break;
        case NodeDeleted:
            log.warn("Warnning! ZK Node for application config has been removed!", null, null);
            break;
        default:
            log.info("Zk Node changed", "Zk Node changed, type" + event.getType() + " Stat:" + event.getState() + ".", null);
            break;
        }
    }
}
