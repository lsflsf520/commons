package com.yisi.stiku.cache.redis;


import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.cache.constant.JedisKeyNS;

/**
 * Created with IntelliJ IDEA.
 * User: jiawuwu
 * Date: 13-10-9
 * Time: 下午5:13
 * To change this template use File | Settings | File Templates.
 */
public class TimeLog {

    private TimeLog(){

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLog.class);

    public static  void log(JedisKeyNS key,Serializable id,long beginTime,String append){
        long nano = System.nanoTime()-beginTime;
        LOGGER.debug(key+" "+id+" useTime="+nano+"(ns),"+(nano/1000000L)+"(ms) "+append );
    }

}
