package com.yisi.stiku.cache.redis;



/**
 * 
 * @author liushangfeng
 *
 */
public class ServerInfo {

	private String name;
	private String host="localhost";
	private int port=6739;
	private int weight=1;
	private String password;
	private int timeout=2000;


    public ServerInfo(){
    }

    public ServerInfo(String host, int port){
        this.host = host;
        this.port = port;
    }
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(name!=null){
			this.name = name;
		}
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		if(host!=null){
			this.host = host;
		}
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public String toString() {
		return this.getName()+"("+this.getHost()+":"+this.getPort()+"/"+this.getPassword()+"/"+this.getWeight()+"/"+this.getTimeout()+")";
	}
	
}
