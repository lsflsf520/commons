package com.yisi.stiku.rpc.assist;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.yisi.stiku.rpc.bean.RpcNode;
import com.yisi.stiku.rpc.cluster.Registry;
import com.yisi.stiku.rpc.util.ZkRpcNodeInfoUtil;

@Service
@SuppressWarnings("all")
public class RpcAssistService implements ApplicationContextAware{
	
	private ApplicationContext context;

	public Set<String> searchServices(String keyword){
		Registry registry = context.getBean(Registry.class);
		Set<String> services = registry.loadCurrServices();
		Set<String> matchingServices = new HashSet<String>();
		for(String service : services){
			if(service.toLowerCase().contains(keyword.toLowerCase())){
				matchingServices.add(service);
			}
		}
		
		return matchingServices;
	}
	
	public boolean exists(String service, String version){
		Registry registry = context.getBean(Registry.class);
		
		String key = ZkRpcNodeInfoUtil.buildServiceKey(service, version);
		
		return registry.loadCurrServices().contains(key);
	}
	
	public List<RpcNode> loadNodes4Service(String service, String version){
		Registry registry = context.getBean(Registry.class);
		
		String key = ZkRpcNodeInfoUtil.buildServiceKey(service, version);
		
		return registry.loadNodes4Service(key);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}
 	
}
