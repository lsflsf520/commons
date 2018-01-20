package com.ujigu.secure.cache.redis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.ujigu.secure.cache.constant.JedisKeyNS;
import com.ujigu.secure.cache.constant.JedisKeyType;
import com.ujigu.secure.cache.exception.JedisClientException;
import com.ujigu.secure.common.utils.BaseConfig;

/**
 * @author jiawu.wu
 */
public class ShardJedisTool {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(ShardJedisTool.class);

	// private static ShardJedisTool instance = new ShardJedisTool();
	private static JedisExecutor executor;

	static {
//		try {
//			executor = JedisExecutor.initInstance(JedisKeyNS.GROUP_NODE);
//		} catch (Exception e) {
//			LOGGER.error("init jedis client from zk config path '"
//					+ JedisKeyNS.GROUP_NODE + "'", e);
//		}
		
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setHost(BaseConfig.getValue("redis.host", "127.0.0.1"));
		serverInfo.setPassword(BaseConfig.getValue("redis.auth.password"));
		serverInfo.setPort(Integer.valueOf(BaseConfig.getValue("redis.port", "6379")));
		executor = JedisExecutor.initInstance(serverInfo);
	}

	@SuppressWarnings({ "all" })
	private static Map<String, String> ckAndFatMap(Map map)
			throws JedisClientException {
		if (map == null || map.isEmpty()) {
			throw new JedisClientException("401", "map is not allow null");
		}
		Set<Map.Entry> entries = map.entrySet();
		for (Map.Entry entry : entries) {
			Object key = entry.getKey();
			if (key == null || key.toString().trim().equals("")) {
				throw new JedisClientException("402", "map's is not allow null");
			}
			Object value = entry.getValue();
			value = value == null ? "" : value.toString();
			map.put(key, value);
		}
		return map;
	}

	private static void ckStringArray(String... members) throws JedisClientException {
		if (members == null || members.length == 0) {
			throw new JedisClientException("401", "members is not allow null");
		}
		for (String member : members) {
			if (member == null) {
				throw new JedisClientException("401",
						"members is not allow null");
			}
		}
	}

	/**
	 * 设置过期时间
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param seconds
	 *            过期时间，单位秒
	 * @return 布尔值 true为设置成功
	 * @throws JedisClientException
	 */
	public static boolean expire(JedisKeyNS key, Serializable id, int seconds)
			throws JedisClientException {

		RedisReturn<Long> rs = new RedisReturn<Long>();

		try {
			rs = executor.expire(key, id, seconds);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.expire: {jedisKey:" + key + ",id:" + id
					+ ",seconds:" + seconds + "}", e);
		}

		// if (key.isPersistNeeded()) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().expire(key, id, seconds));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.expire: {jedisKey:" + key + ",id:"
		// + id + ",seconds:" + seconds + "}", null);
		// }
		// }

		return !rs.isAllDown();
	}

	/**
	 * 判断是否存在
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return 布尔值 true为存在
	 * @throws JedisClientException
	 */
	public static boolean exists(JedisKeyNS key, Serializable id)
			throws JedisClientException {

		RedisReturn<Boolean> rs = new RedisReturn<Boolean>();
		try {
			rs = executor.exists(key, id);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.exists: {JedisKeyNS:" + key + ",id:" + id
					+ "}", e);
		}

		// if (key.isPersistNeeded()) {
		// if (rs.isAllDown()
		// || (rs.getOperRs() == null && key.isPersistQuery())) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().exists(key,
		// id.toString()));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.exists: {JedisKeyNS:" + key + ",id:"
		// + id + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();

	}

	/**
	 * 判断Hash（key－value）列表中某个指定的key是否存在
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param field
	 *            Hash（key－value）列表中的key
	 * @return 布尔值 true－存在
	 * @throws JedisClientException
	 */
	public static boolean hexists(JedisKeyNS key, Serializable id, String field)
			throws JedisClientException {

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hexists) only can operate "
							+ JedisKeyType.HASH + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<Boolean> rs = new RedisReturn<Boolean>();

		try {
			rs = executor.hexists(key, id, field);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.hexists: {JedisKeyNS:" + key + ",id:" + id
					+ ",field:" + field + "}", e);
		}

		// if (key.isPersistNeeded()) {
		// if (rs.isAllDown()
		// || (rs.getOperRs() == null && key.isPersistQuery())) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().hexists(key,
		// id.toString(), field));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.hexists: {JedisKeyNS:" + key + ",id:"
		// + id + ",field:" + field + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();

	}

	/**
	 * 追加字符串
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param value
	 *            需要追加的字符串
	 * @return 布尔值 true－追加成功
	 * @throws JedisClientException
	 */
	public static boolean append(JedisKeyNS key, Serializable id, Object value)
			throws JedisClientException {

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (append) only can operate "
							+ JedisKeyType.STRING + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}
		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (append) does not support Persist. key="
		// + key.name());
		// }
		RedisReturn<Long> rs = new RedisReturn<Long>();
		try {
			rs = executor.append(key, id, value);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.append: {JedisKeyNS:" + key + ",id:" + id
					+ ",value:" + value + "}", e);
		}

		if (rs.isAllDown()) {
			throw new JedisClientException("ERROR_500", " exec append: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",value:" + value + "} ");
		}

		return !rs.isAllDown();
	}

	/**
	 * 将原值覆盖为value.toString()的结果，如果Redis里面实际的KEY不存在，则插入。
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param value
	 *            要set的值
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean set(JedisKeyNS key, Serializable id, Object value)
			throws JedisClientException {

//		long accessTime = System.nanoTime();

		if (value == null) {
			throw new JedisClientException("400", " value not allow null");
		}

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this (set) method does not fixed keyType. key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

//		TimeLog.log(key, id, accessTime,
//				"past check ShardJedisTool.set params.");

		RedisReturn<String> rs = new RedisReturn<String>();

		try {
			rs = executor.set(key, id, value);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.set: {JedisKeyNS:" + key + ",id:" + id
					+ ",value:" + value + "}", e);
		}

		boolean lastResult = !rs.isAllDown();

		// 持久化的 ， 把请求转到 mongoDb
		// if (key.isPersistNeeded()) {
		// try {
		// lastResult = MongoClient.getInstance().set(key, id.toString(),
		// value.toString());
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.set: {key:" + key + ",id:" + id
		// + ",value:" + value + "}", null);
		// }
		// }

//		TimeLog.log(key, id, accessTime,
//				"finish execute ShardJedisTool.set function.");

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec set: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id
					+ ",value:" + value + "} ");
		}

		return lastResult;

	}
	
	/**
	 * 将原值覆盖为value.toString()的结果，如果Redis里面实际的KEY不存在，则插入。
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param value
	 *            要set的值
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean setIfAbsent(JedisKeyNS key, Serializable id, Object value)
			throws JedisClientException {
		if (value == null) {
			throw new JedisClientException("400", " value not allow null");
		}

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this (set) method does not fixed keyType. key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<Long> rs = new RedisReturn<Long>();

		try {
			rs = executor.setIfAbsent(key, id, value);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.set: {JedisKeyNS:" + key + ",id:" + id
					+ ",value:" + value + "}", e);
		}

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec set: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id
					+ ",value:" + value + "} ");
		}

		return lastResult && rs.getOperRs() > 0;

	}

	/**
	 * 根据KEY取值
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return KEY关联的值
	 * @throws JedisClientException
	 */
	public static String get(JedisKeyNS key, Serializable id)
			throws JedisClientException {

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (get) only can operate " + JedisKeyType.STRING
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		RedisReturn<String> rs = new RedisReturn<String>();

		try {
			rs = executor.get(key, id);
		} catch (JedisConnectionException e) {
			LOGGER.error(
					"executor.get: {JedisKeyNS:" + key + ",id:" + id + "}", e);
		}

		// if (key.isPersistNeeded()) {
		// if (rs.isAllDown()
		// || (rs.getOperRs() == null && key.isPersistQuery())) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().get(key,
		// id.toString()));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.get: {JedisKeyNS:" + key + ",id:" + id
		// + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();
	}

	/**
	 * 先取出值，再覆盖为newValue.toString()的结果。如果KEY不存在，则插入。
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param newValue
	 *            要覆盖为的对象
	 * @return 操作前的旧值
	 * @throws JedisClientException
	 */
	public static String getSet(JedisKeyNS key, Serializable id, Object newValue)
			throws JedisClientException {

		if (newValue == null || newValue.toString().trim().equals("")) {
			throw new JedisClientException("400", " newValue not allow null");
		}

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (getSet) only can operate "
							+ JedisKeyType.STRING + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<String> rs = new RedisReturn<String>();

		try {
			rs = executor.getSet(key, id, newValue);
		} catch (JedisConnectionException e) {
			LOGGER.error("executor.getSet: {JedisKeyNS:" + key + ",id:" + id
					+ "}", e);
		}

		String oldVal = rs.getOperRs();

		// if (key.isPersistNeeded()) {
		// if (rs.isAllDown()
		// || (StringUtils.isBlank(oldVal) && key.isPersistQuery())) {
		// try {
		// oldVal = MongoClient.getInstance().getSet(key,
		// id.toString(), newValue.toString());
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.getSet: {JedisKeyNS:" + key + ",id:"
		// + id + ",newValue:" + newValue + "}", null);
		// }
		// }
		// }

		return oldVal;
	}

	/**
	 * 删除KEY及关联的数据
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean del(JedisKeyNS key, Serializable id)
			throws JedisClientException {

//		long start = System.currentTimeMillis();

		RedisReturn<Long> rs = executor.del(key, id);

		boolean lastResult = !rs.isAllDown();

		// if (key.isPersistNeeded()) {
		// try {
		// lastResult = MongoClient.getInstance().del(key, id.toString());
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.del: {JedisKeyNS:" + key + ",id:" + id +
		// "}", null);
		// }
		// }

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec del: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id
					+ "} ");
		}

//		TimeLog.log(key, id, start,
//				"===> useTime: " + (System.currentTimeMillis() - start));

		return lastResult;

	}
	
	/**
	 * 该方法将直接以key作为redis的key，慎用！
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @throws JedisClientException
	 */
//	public static boolean hset(JedisKeyNS key, String field,
//			Object value) throws JedisClientException {
//		return hset(key, 0, field, value);
//	}
	
	/**
	 * 该方法与上边的hset方法一一对应
	 * @param key
	 * @param field
	 * @return
	 */
//	public static String hget(JedisKeyNS key, String field){
//		return hget(key, 0, field);
//	}
	
	/**
	 * 该方法与上边的hset方法一一对应
	 * @param key
	 * @return 
	 */
//	public static boolean delHKey(JedisKeyNS key){
//		return del(key, 0);
//	}
	
	/**
	 * 该方法与上边的hset方法一一对应
	 * @param key
	 * @return 
	 */
//	public static boolean hdel(JedisKeyNS key, String... fields){
//		return hdel(key, 0, fields);
//	}

	/**
	 * 将KEY关联的哈希表中的域 field 的值设为 value
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param field
	 *            哈希表中的key
	 * @param value
	 *            哈希表中的value
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean hset(JedisKeyNS key, Serializable id, String field,
			Object value) throws JedisClientException {

		if (field == null || field.toString().trim().equals("")) {
			throw new JedisClientException("400", " field not allow null");
		}

		if (value == null) {
			value = "";
		}

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hset) only can operate " + JedisKeyType.HASH
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		RedisReturn<Long> rs = executor.hset(key, id, field, value);

		boolean lastResult = !rs.isAllDown();

		// 所有redis down 的情况 以及 是 持久化的 ， 把请求转到 mongoDb
		// if (key.isPersistNeeded()) {
		// try {
		// lastResult = MongoClient.getInstance().hset(key, id.toString(),
		// field, value.toString());
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.hset: {JedisKeyNS:" + key + ",id:" + id
		// + ",field:" + field + ",value:" + value + "}", null);
		// }
		// }

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec hset: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",field:" + field + ",value:" + value + "} ");
		}

		return lastResult;
	}

	/**
	 * 获取KEY关联的哈希表中指定的域field的value
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param field
	 *            哈希表中指定的域
	 * @return 哈希表field关联的value
	 * @throws JedisClientException
	 */
	public static String hget(JedisKeyNS key, Serializable id, String field)
			throws JedisClientException {

		if (field == null || field.trim().equals("")) {
			throw new JedisClientException("400", " field not allow null");
		}

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hget) only can operate " + JedisKeyType.HASH
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		RedisReturn<String> rs = executor.hget(key, id, field);

		// if (key.isPersistNeeded()) {
		// if (rs.isAllDown()
		// || (rs.getOperRs() == null && key.isPersistQuery())) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().hget(key,
		// id.toString(), field));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.hget: {JedisKeyNS:" + key + ",id:" + id
		// + ",field:" + field + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();

	}
	
	public static long hincrBy(JedisKeyNS key, Serializable id, String field,int value){
		if (field == null || field.trim().equals("")) {
			throw new JedisClientException("400", " field not allow null");
		}

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hget) only can operate " + JedisKeyType.HASH
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}
		
		RedisReturn<Long> rs =executor.hincrBy(key, id, field, value);
		return rs.getOperRs();
	}

	/**
	 * 获取KEY关联的哈希表
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return KEY关联的哈希表
	 * @throws JedisClientException
	 */
	public static Map<String, String> hgetAll(JedisKeyNS key, Serializable id)
			throws JedisClientException {

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hgetAll) only can operate "
							+ JedisKeyType.HASH + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<Map<String, String>> rs = executor.hgetAll(key, id);

		// if (key.isPersistNeeded()) {
		// if (rs.isAllDown()
		// || ((rs.getOperRs() == null || rs.getOperRs().isEmpty()) && key
		// .isPersistQuery())) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().hgetAll(key,
		// id.toString()));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.hgetAll: {JedisKeyNS:" + key + ",id:"
		// + id + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();

	}

	/**
	 * 删除KEY关联的哈希表中的域
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param fields
	 *            哈希表中要删除的域
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean hdel(JedisKeyNS key, Serializable id, String... fields)
			throws JedisClientException {

		ckStringArray(fields);

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hdel) only can operate " + JedisKeyType.HASH
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// try {
		// MongoClient.getInstance().hdel(key, id, fields);
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.hdel: {JedisKeyNS:" + key + ",id:" + id
		// + ",fields:" + fields + "}", null);
		// }
		// }

		RedisReturn<Long> rs = executor.hdel(key, id, fields);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec hset: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",field:" + fields + "} ");
		}

		return lastResult;

	}

	/**
	 * 覆盖KEY关联的哈希表
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param map
	 *            要覆盖的哈希表
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean hmset(JedisKeyNS key, Serializable id,
			Map<String, Object> map) throws JedisClientException {

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hmset) only can operate " + JedisKeyType.HASH
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		RedisReturn<String> rs = executor.hmset(key, id, ckAndFatMap(map));

		boolean lastResult = !rs.isAllDown();

		// if (key.isPersistNeeded()) {
		// try {
		// lastResult = MongoClient.getInstance().hmset(key, id, map);
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.hmset: {JedisKeyNS:" + key + ",id:" + id
		// + ",map:" + map + "}", null);
		// }
		// }

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec hmset: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",map:" + map + "} ");
		}

		return lastResult;

	}

	/**
	 * 获取KEY关联的哈希表中指定的域列表对应的value列表
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param fields
	 *            哈希表中的域列表
	 * @return 哈希表中的value列表中
	 * @throws JedisClientException
	 */
	public static List<String> hmget(JedisKeyNS key, Serializable id, String... fields)
			throws JedisClientException {

		ckStringArray(fields);

		if (!JedisKeyType.HASH.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (hmget) only can operate " + JedisKeyType.HASH
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (hmget) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<List<String>> rs = executor.hmget(key, id, fields);

		return rs.getOperRs();
	}

	/**
	 * 往KEY关联的SET集合增加成员
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param members
	 *            要增加的成员列表
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean sadd(JedisKeyNS key, Serializable id, String... members)
			throws JedisClientException {

		ckStringArray(members);

		if (!JedisKeyType.SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (sadd) only can operate " + JedisKeyType.SET
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		RedisReturn<Long> rs = executor.sadd(key, id, members);

		boolean lastResult = !rs.isAllDown();

		// if (key.isPersistNeeded()) {
		// try {
		// lastResult = MongoClient.getInstance().sadd(key, id.toString(),
		// members);
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.sadd: {JedisKeyNS:" + key + ",id:" + id
		// + ",members:" + Arrays.asList(members) + "}", null);
		// }
		// }

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec sadd: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",members:" + Arrays.asList(members) + "} ");
		}

		return lastResult;
	}

	/**
	 * 返回kEY关联的SET集合
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return kEY关联的SET集合
	 * @throws JedisClientException
	 */
	public static Set<String> smembers(JedisKeyNS key, Serializable id)
			throws JedisClientException {

		if (!JedisKeyType.SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (smembers) only can operate "
							+ JedisKeyType.SET + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<Set<String>> rs = executor.smembers(key, id);

		// if (key.isPersistNeeded()) {

		// if (rs.isAllDown()
		// || (key.isPersistQuery() && (rs.getOperRs() == null || rs
		// .getOperRs().size() == 0))) {
		// try {
		// rs.setOperRs(MongoClient.getInstance().smembersSet(key, id));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.smembersSet: {JedisKeyNS:" + key.name()
		// + ",id:" + id + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();
	}

	public static boolean sismember(JedisKeyNS key, Serializable id, Object value)
			throws JedisClientException {
		if (!JedisKeyType.SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (smembers) only can operate "
							+ JedisKeyType.SET + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		if (value == null) {
			return false;
		}

		RedisReturn<Boolean> rs = executor.sismember(key, id, value.toString());
		// if (key.isPersistNeeded()) {
		// if(rs.isAllDown()
		// || (key.isPersistQuery() && (rs.getOperRs() == null))){
		// try {
		// rs.setOperRs(MongoClient.getInstance().sismember(key, id,
		// value.toString()));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.sismember: {JedisKeyNS:" + key.name()
		// + ",id:" + id + ",value:" + value + "}", null);
		// }
		// }

		// }

		return rs.getOperRs() == null ? false : rs.getOperRs();
	}

	public static int scard(JedisKeyNS key, Serializable id)
			throws JedisClientException {
		if (!JedisKeyType.SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (smembers) only can operate "
							+ JedisKeyType.SET + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<Long> rs = executor.scard(key, id);
		// if (key.isPersistNeeded()) {
		// if(rs.isAllDown()
		// || (key.isPersistQuery() && (rs.getOperRs() == null))){
		// try {
		// rs.setOperRs(MongoClient.getInstance().scard(key, id));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.sismember: {JedisKeyNS:" + key.name()
		// + ",id:" + id + "}", null);
		// }
		// }

		// }

		return rs.getOperRs() == null ? 0 : rs.getOperRs().intValue();
	}

	/**
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return 从Set数据结构中随机选择一个元素返回
	 * @throws JedisClientException
	 */
	public static String srandmember(JedisKeyNS key, Serializable id)
			throws JedisClientException {
		if (!JedisKeyType.SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (smembers) only can operate "
							+ JedisKeyType.SET + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		RedisReturn<String> rs = executor.srandmember(key, id);
		// if (key.isPersistNeeded()) {
		// if(rs.isAllDown()
		// || (key.isPersistQuery() && (rs.getOperRs() == null))){
		// try {
		// rs.setOperRs(MongoClient.getInstance().srandmember(key, id));
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.sismember: {JedisKeyNS:" + key.name()
		// + ",id:" + id + "}", null);
		// }
		// }
		// }

		return rs.getOperRs();
	}

	/**
	 * 删除KEY关联的SET集合中指定的成员members
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param members
	 *            要删除的成员
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean srem(JedisKeyNS key, Serializable id, String... members)
			throws JedisClientException {

		ckStringArray(members);

		if (!JedisKeyType.SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (srem) only can operate " + JedisKeyType.SET
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		RedisReturn<Long> rs = executor.srem(key, id, members);

		boolean lastResult = !rs.isAllDown();

		// if (key.isPersistNeeded()) {
		// try {
		// lastResult = MongoClient.getInstance().srem(key, id, members);
		// } catch (Exception e) {
		// LOGGER.error(e, "MongoClient.srem: {JedisKeyNS:" + key.name() +
		// ",id:"
		// + id + ",members:" + Arrays.asList(members)
		// + "}", null);
		// }
		// }

		if (!lastResult) {
			throw new JedisClientException("ERROR_500","exec srem: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",members:" + Arrays.asList(members) + "} ");
		}

		return lastResult;
	}

	/**
	 * 返回KEY关联的SET集合排序后的结果 （默认排序规则）
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return KEY关联的SET集合排序后的结果
	 * @throws JedisClientException
	 */
	public static List<String> sort(JedisKeyNS key, Serializable id)
			throws JedisClientException {
		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (sort) does not support Persist. key="
		// + key.name());
		// }
		RedisReturn<List<String>> rs = executor.sort(key, id);
		return rs.getOperRs();
	}

	/**
	 * 返回KEY关联的SET集合排序后的结果 （params指定的排序规则）
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param params
	 *            排序规则
	 * @return KEY关联的SET集合排序后的结果
	 * @throws JedisClientException
	 */
	public static List<String> sort(JedisKeyNS key, Serializable id,
			SortingParams params) throws JedisClientException {

		if (params == null) {
			throw new JedisClientException("400", " sort params not allow null");
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (sort) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<List<String>> rs = executor.sort(key, id, params);

		return rs.getOperRs();
	}

	/**
	 * 往KEY关联的有序SET集合增加value成员(字符串)
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param score
	 *            分数
	 * @param value
	 *            要往SET中增加的成员
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean zadd(JedisKeyNS key, Serializable id, double score,
			Object value) throws JedisClientException {

		if (value == null || value.toString().trim().equals("")) {
			throw new JedisClientException("400", " zadd value not allow null");
		}

		if (!JedisKeyType.SORTED_SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (zadd) only can operate "
							+ JedisKeyType.SORTED_SET
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (zadd) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<Long> rs = executor.zadd(key, id, score, value);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec zadd: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",score:" + score + ",value=" + value + "} ");
		}

		return lastResult;
	}

	/**
	 * 往KEY关联的有序SET集合增加一个Map对象
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param scoreMembers
	 *            要往SET添加的map对象
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	// public boolean zadd(JedisKeyNS key, Serializable id,
	// Map<Double, String> scoreMembers) throws JedisClientException {
	//
	// if (scoreMembers == null || scoreMembers.size() == 0) {
	// throw new JedisClientException("400",
	// " zadd scoreMembers not allow null");
	// }
	//
	// if (!JedisKeyNS.SORTED_SET.equals(key.getKeyType())) {
	// throw new JedisClientException("404",
	// "this method (zadd) only can operate "
	// + JedisKeyNS.SORTED_SET + ". but found this -> key="
	// + key.name() + " key.keyType=" + key.getKeyType());
	// }
	//
	// if (key.isPersistNeeded()) {
	// throw new JedisClientException("405",
	// "this method (zadd) does not support Persist. key="
	// + key.name());
	// }
	//
	// RedisReturn<Long> rs = executor.zadd(key, id, scoreMembers);
	//
	// boolean lastResult = !rs.isAllDown();
	//
	// if (!lastResult) {
	// throw new JedisClientException(JedisClientExceptionCode.ERROR_500,
	// this.getClass().getSimpleName() + ".zadd: {JedisKeyNS:" + key
	// + ",id:" + id + ",scoreMembers:" + scoreMembers
	// + "} ");
	// }
	//
	// return lastResult;
	// }

	/**
	 * 往KEY关联的有序SET集合删除members成员
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param members
	 *            要删除的成员
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static boolean zrem(JedisKeyNS key, Serializable id, String... members)
			throws JedisClientException {

		ckStringArray(members);

		if (!JedisKeyType.SORTED_SET.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (zrem) only can operate "
							+ JedisKeyType.SORTED_SET
							+ ". but found this -> key=" + key.getNameSpace()
							+ " key.keyType=" + key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (zrem) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<Long> rs = executor.zrem(key, id, members);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec zrem: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",members:" + Arrays.asList(members) + "} ");
		}

		return lastResult;
	}

	/**
	 * 将KEY关联的String值自减1
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static long decr(JedisKeyNS key, Serializable id)
			throws JedisClientException {

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (decr) only can operate "
							+ JedisKeyType.STRING + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (decr) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<Long> rs = executor.decr(key, id);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec decr: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + "} ");
		}

		return rs.getOperRs();
	}

	/**
	 * 将KEY关联的String值自减byvalue值
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param byvalue
	 *            要自减的值
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static long decrBy(JedisKeyNS key, Serializable id, int byvalue)
			throws JedisClientException {

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (decrBy) only can operate "
							+ JedisKeyType.STRING + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (decrBy) does not support Persist. JedisKeyNS="
		// + key.name());
		// }

		RedisReturn<Long> rs = executor.decrBy(key, id, byvalue);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec decr: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + "} ");
		}

		return rs.getOperRs();
	}

	/**
	 * 将KEY关联的String值自减1
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static long incr(JedisKeyNS key, Serializable id)
			throws JedisClientException {

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (incr) only can operate "
							+ JedisKeyType.STRING + ". but found this -> key="
							+ key.getNameSpace() + " key.getKeyType="
							+ key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (incr) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<Long> rs = executor.incr(key, id);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec incr: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + "} ");
		}

		return rs.getOperRs();
	}
	
	/**
	 * 将KEY关联的String值自减将KEY关联的String值自减1
	 * 
	 * @param key
	 *            跟id参数一起组成Redis里面实际的KEY
	 * @param id
	 *            跟key参数一起组成Redis里面实际的KEY
	 * @param byvalue
	 *            要自增的值
	 * @return 布尔值 true－操作成功
	 * @throws JedisClientException
	 */
	public static long incrBy(JedisKeyNS key, Serializable id, int byvalue)
			throws JedisClientException {

		if (!JedisKeyType.STRING.equals(key.getKeyType())) {
			throw new JedisClientException("404",
					"this method (incrBy) only can operate "
							+ JedisKeyType.STRING + ". but found this -> key="
							+ key.getNameSpace() + " key.keyType="
							+ key.getKeyType());
		}

		// if (key.isPersistNeeded()) {
		// throw new JedisClientException("405",
		// "this method (incrBy) does not support Persist. key="
		// + key.name());
		// }

		RedisReturn<Long> rs = executor.incrBy(key, id, byvalue);

		boolean lastResult = !rs.isAllDown();

		if (!lastResult) {
			throw new JedisClientException("ERROR_500", "exec incrBy: {JedisKeyNS:"
					+ key
					+ ",id:"
					+ id + ",byvalue:" + byvalue + "} ");
		}

		return rs.getOperRs();
	}

}
