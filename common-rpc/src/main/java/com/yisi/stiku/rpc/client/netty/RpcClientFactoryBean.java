package com.yisi.stiku.rpc.client.netty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.log.LogUtil;
import com.yisi.stiku.rpc.bean.RpcConstants;
import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.bean.RpcRequest;
import com.yisi.stiku.rpc.bean.RpcResponse;
import com.yisi.stiku.rpc.cluster.Router;

@SuppressWarnings("all")
public class RpcClientFactoryBean implements FactoryBean{
	
	private final static Logger LOG = LoggerFactory.getLogger(RpcClientFactoryBean.class);
	private volatile long lastCallTime; //单位(毫秒)，记录上一次调用该类中任一远程方法的时间，以便负载均衡计算

    private String rpcInterface;
    private String version;
//    private int idleTime = RpcConstant.DEFAULT_IDLE_TIME; //单位(秒)，如果超过idleTime(秒)时间该类的任一远程方法均未被使用，则在下一次调用该类的远程方法时将重新选择服务节点
    private Router router;

    public RpcClientFactoryBean(Router router, String rpcInterface){
    	this.router = router;
    	this.rpcInterface = rpcInterface;
    }
    
//    public RpcClientFactoryBean(String rpcInterface){
//    	this.rpcInterface = rpcInterface;
//    }

    @SuppressWarnings("unchecked")
    private <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                	
                	return RpcClientUtil.sendRequest(router, method.getDeclaringClass(), getVersion(), method.getName(), args, method.getParameterTypes());
//                    RpcRequest request = new RpcRequest(method.getDeclaringClass(), getVersion(), method.getName(), args); // 创建并初始化 RPC 请求
//                    request.setParameterTypes(method.getParameterTypes());
//                    
//                    request.putAll(ThreadUtil.getThreadInfoMap());
//                    request.addExtraInfo(RpcConstants.REQUEST_PROJECT, ZkConstant.PROJECT_NAME);
//                    
//                    long start = System.currentTimeMillis();
//                    
//                    RpcResponse response = router.send(request);
//                    
//                    LogUtil.xnLog(LogUtil.RPC_CLIENT_TYPE, method.getDeclaringClass().getName() + "." + method.getName(), System.currentTimeMillis() - start);
//                    if(response.isError()){
//                    	throw response.getException();
//                    }
//                    
//                    return response.getReturnValue();
                }
            }
        );
    }

	@Override
	public Object getObject() throws Exception {
		return create(getObjectType());
	}

	@Override
	public Class getObjectType() {
		if(StringUtils.isBlank(rpcInterface)){
		    LOG.warn("there has null rpcInterface to be loaded, system will abandon it.");	
		    return null;
		}
		
		try {
			return Class.forName(rpcInterface);
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return null;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public String getVersion() {
		return StringUtils.isEmpty(version) ? "1.0.0" : version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
