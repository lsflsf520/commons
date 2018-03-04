package com.xyz.tools.cache.redis;

/**
 * 
 * @author lsf
 *
 * @param <T>
 */
public class RedisReturn<T>{

    private boolean isAllDown; // redis 是否全部down
    private T operRs; // redis命令返回值

    public RedisReturn() {

    }

    public RedisReturn(boolean isAllDown) {
		this.isAllDown = isAllDown;
	}
    
    public boolean isAllDown() {
        return isAllDown;
    }

    public T getOperRs() {
        return operRs;
    }

    public void setOperRs(T operRs) {
        this.operRs = operRs;
    }
}