package com.xyz.tools.cache.redis;


import redis.clients.jedis.Jedis;

import java.io.Serializable;

import com.xyz.tools.cache.constant.ExtOperType;
import com.xyz.tools.cache.constant.JedisKeyNS;


/**
 * 
 * @author liushangfeng
 *
 * @param <T> 定义返回类型
 */
public abstract class  ExtOper<T> {

    private ExtOperType operType;
    private JedisKeyNS key;
    private Serializable id;


    public ExtOper(ExtOperType operType){
        this.operType = operType;
    }

    public ExtOper(ExtOperType operType,JedisKeyNS key,Serializable id){
        this(operType);
        this.key = key;
        this.id=id;
    }

    public ExtOperType getExOperType(){
         return operType;
    }

    public JedisKeyNS getKey() {
        return key;
    }

    public Serializable getId() {
        return id;
    }

	/**
	 * 
	 * <Description>this is a method</Description>
	 *
	 * @param jedis 回调函数的Jedis对象
	 * @param keystr jedis的键
	 * @param middleKey 只有hset、zadd这种类型，需要有一个中间域标识的数据类型才有值，其它均为null
	 * @param values jedis的键
	 * @return 返回指定类型的对象
	 */
 	  abstract  T  exec(Jedis jedis,String keystr,Object middleKey,String... values);




	
}
