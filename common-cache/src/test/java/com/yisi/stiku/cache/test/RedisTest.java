package com.yisi.stiku.cache.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import com.yisi.stiku.cache.constant.DefaultJedisKeyNS;
import com.yisi.stiku.cache.constant.JedisKeyNS;
import com.yisi.stiku.cache.constant.JedisKeyType;
import com.yisi.stiku.cache.redis.ShardJedisTool;


public class RedisTest {

	public static void main(String[] args) {
//		boolean exist = ShardJedisTool.exists(DefaultJedisKeyNS.testKey, 123);
//		System.out.println("exist " + exist);
		
//		boolean success = ShardJedisTool.set(DefaultJedisKeyNS.testKey, 123, "hahhaha");
//		System.out.println(success);
		
//		String value = ShardJedisTool.get(DefaultJedisKeyNS.testKey, 123);
//		System.out.println(value);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("height", "234");
		map.put("age", "26");
		ShardJedisTool.hmset(DefaultJedisKeyNS.asyncJobAttr, "12jj2323", map);
		ShardJedisTool.hincr(DefaultJedisKeyNS.asyncJobAttr, "12jj2323","age");
		ShardJedisTool.hincrby(DefaultJedisKeyNS.asyncJobAttr, "12jj2323","height",111);
//		
		Map<String, String> cacheMap = ShardJedisTool.hgetAll(DefaultJedisKeyNS.asyncJobAttr, "12jj2323");
		System.out.println(cacheMap);
		
//		boolean result = ShardJedisTool.lpush(DefaultJedisKeyNS.asyncTaskDetail, "listtest", "123");
//		System.out.println("lpush:"+result);
//		result = ShardJedisTool.rpush(DefaultJedisKeyNS.asyncTaskDetail, "listtest", "456");
//		System.out.println("rpush:"+result);
//		result = ShardJedisTool.rpush(DefaultJedisKeyNS.asyncTaskDetail, "listtest", "89");
//		System.out.println("rpush:"+result);
//
//		long val = ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("llen:"+val);
//
//		List<String> valList = ShardJedisTool.lrange(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0,1);
//		System.out.println("lrange:"+valList);
//
//		result = ShardJedisTool.ltrim(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0,1);
//		System.out.println("ltrim:"+result);
//		val = ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("after ltrim,the length="+val);
//
//		boolean result1 =  ShardJedisTool.lset(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0,"10000");
//		System.out.println("lset:"+result1);
//
//		String sVal = ShardJedisTool.lindex(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0);
//		System.out.println("lindex:"+sVal);
//
//		valList = ShardJedisTool.lrange(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0,ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest")-1);
//		System.out.println("lrange:"+valList);
//
//		boolean result2 =  ShardJedisTool.lrem(DefaultJedisKeyNS.asyncTaskDetail, "listtest",1,"456");
//		System.out.println("lrem:"+result2);
//		val = ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("after lrem,the length="+val);
//
//		sVal = ShardJedisTool.lpop(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("lpop:"+sVal);
//
//		sVal = ShardJedisTool.rpop(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("rpop:"+sVal);
//
//		val = ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("after rpop,the length="+val);


//		List<Object> resultList = new ArrayList<Object>();
//		resultList.add("1");
//		resultList.add("2");
//		resultList.add("3");
//		resultList.add("4");
//		resultList.add("5");
//		resultList.add("6");
//		boolean result2 = ShardJedisTool.lPutListToHead(DefaultJedisKeyNS.asyncTaskDetail, "listtest",resultList);
//		System.out.println("lPutListToHead:"+result2);
//		List<String> valList = ShardJedisTool.lrange(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0,ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest")-1);
//		System.out.println("lrange:"+valList);
//		valList = ShardJedisTool.lTakeListFromTailToHead(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("lTakeListFromHeadToTail:"+valList);
//
//		List<Object> resultList2 = new ArrayList<Object>();
//		resultList2.add("A");
//		resultList2.add("B");
//		resultList2.add("C");
//		resultList2.add("D");
//		resultList2.add("E");
//		resultList2.add("F");
//		result2 = ShardJedisTool.lPutListToTail(DefaultJedisKeyNS.asyncTaskDetail, "listtest",resultList2);
//		System.out.println("lPutListToTail:"+result2);
//		valList = ShardJedisTool.lrange(DefaultJedisKeyNS.asyncTaskDetail, "listtest",0,ShardJedisTool.llen(DefaultJedisKeyNS.asyncTaskDetail, "listtest")-1);
//		System.out.println("lPutListToTail_lrange:"+valList);
//
//		valList = ShardJedisTool.lTakeListFromHeadToTail(DefaultJedisKeyNS.asyncTaskDetail, "listtest");
//		System.out.println("lTakeListFromTailToHead:"+valList);
	}

}
