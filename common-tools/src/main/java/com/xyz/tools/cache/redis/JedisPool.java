package com.xyz.tools.cache.redis;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


/**
 *
 *
 * User: jiawuwu
 * Date: 13-10-9
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
public class JedisPool extends redis.clients.jedis.JedisPool{

    private String host;
    private int port;
    private String authInfo;

    public JedisPool(GenericObjectPoolConfig poolConfig, String host, int port) {
        super(poolConfig, host, port);
        this.host = host;
        this.port = port;
    }
    
    public JedisPool(GenericObjectPoolConfig poolConfig, String host, int port, String password) {
    	super(poolConfig, host, port, 5000, password);
    	this.host = host;
        this.port = port;
        this.authInfo = password;
	}

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
	public String getAuthInfo() {
		return authInfo;
	}

	@Override
	public String toString() {
		return "JedisPool [host=" + host + ", port=" + port + "]";
	}
    
}
