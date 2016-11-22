package com.yisi.stiku.cache.constant;


/**
 *
 * Redis数据操作类型
 *
 * User: jiawuwu
 * Date: 13-9-30
 * Time: 下午4:26
 * To change this template use File | Settings | File Templates.
 */
public enum ExtOperType {
   CHANGE,//add，del，set，remove，自增，自减等更改数据的操作类型，这种类型的操作如果缓存是持久化的，则需要更新持久化的数据。
   QUERY,// 读、查询类操作
   EXPIRE //设置过期时间
}
