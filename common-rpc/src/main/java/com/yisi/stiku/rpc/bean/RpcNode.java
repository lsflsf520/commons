package com.yisi.stiku.rpc.bean;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.utils.IPUtil;

/**
 * 
 * @author shangfeng
 *
 */
public class RpcNode{

	private String host;
	private int port;
	private int weight;
	private String version;
	private String group;
	private String module;
	private String project;
	
	private Set<String> services;
	
	public String getHost() {
		if(StringUtils.isBlank(host)){
			this.host = IPUtil.getLocalIp();
		}
		return host;
	}
	public void setHost(String host) {
		if(StringUtils.isNotBlank(this.host)){
			throw new IllegalStateException("host has already set");
		}
		this.host = host;
	}
	public int getPort() {
		if(port <= 0){
			this.port = RpcConstants.DEFAULT_SERVER_PORT;
		}
		return port;
	}
	public void setPort(int port) {
		if(this.port > 0){
			throw new IllegalStateException("port has already set");
		}
		this.port = port;
	}
	public int getWeight() {
		return weight <= 0 ? 1 : weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getVersion() {
		return StringUtils.isEmpty(version) ? "1.0.0" : version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public Set<String> getServices() {
		return services;
	}
	public void setServices(Set<String> services) {
		this.services = services;
	}
	@Override
	public String toString() {
		return "{host:" + host + ",port:" + port + ",vesion:" + version + ",weight:" + weight + "}";
	}
	
	@Override
	public int hashCode() {
		return getHost().hashCode() * 7 + this.getPort() * 7;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RpcNode)){
			return false;
		}
		
		RpcNode node = (RpcNode)obj;
		
		return this.getHost().equals(node.getHost()) && this.getPort() == node.getPort();
	}
}
