package com.yisi.stiku.cache.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import com.yisi.stiku.cache.constant.CacheNameSpace;
import com.yisi.stiku.cache.constant.ExtOperType;
import com.yisi.stiku.cache.constant.JedisKeyNS;
import com.yisi.stiku.cache.exception.JedisClientException;
import com.yisi.stiku.conf.ZKClient;

/**
 * 缓存基本工具类 User: jiawuwu Date: 13-10-1 Time: 下午1:26 To change this template use
 * File | Settings | File Templates.
 */
@SuppressWarnings({ "unchecked" })
public class JedisExecutor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JedisExecutor.class);
	private static JedisPoolConfig config = new JedisPoolConfig();
	private static JedisExecutor executor;

	static {
		config.setMaxTotal(40);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(1000L);;
	}

	private List<JedisPool> jedisPools = new CopyOnWriteArrayList<JedisPool>();
	private Random random = new Random();

	private JedisExecutor(String configFilePath) throws JedisClientException,
			KeeperException, InterruptedException {
		init(configFilePath);
		watchDir(configFilePath);
	}

	private JedisExecutor(ServerInfo... servInfos)
			throws JedisConnectionException, JedisClientException {
		for (ServerInfo serverInfo : servInfos) {
			JedisPool pool = JedisExecutor.getJedisPool(serverInfo.getHost(),
					serverInfo.getPort());
			jedisPools.add(pool);
		}

	}

	public static JedisExecutor initInstance(String configFilePath)
			throws JedisClientException, KeeperException, InterruptedException {
		if (executor == null) {
			synchronized (JedisExecutor.class) {
				if (executor == null) {
					executor = new JedisExecutor(configFilePath);
				}
			}
		}

		return executor;
	}

	public static JedisExecutor initInstance(ServerInfo... servInfos)
			throws JedisConnectionException, JedisClientException {

		if (executor == null) {
			synchronized (JedisExecutor.class) {
				if (executor == null) {
					executor = new JedisExecutor(servInfos);
				}
			}
		}

		return executor;
	}

	private synchronized void init(String configFilePath)
			throws JedisClientException, KeeperException, InterruptedException {
		ConfigUtil.initList(configFilePath);
		List<ServerInfo> rlist = ConfigUtil.getRedisNodes();
		jedisPools = new ArrayList<JedisPool>(10);
		for (ServerInfo serverInfo : rlist) {
			JedisPool pool = JedisExecutor.getJedisPool(serverInfo.getHost(),
					serverInfo.getPort());
			jedisPools.add(pool);
		}
	}

	/**
	 * 服务节点列表监控器
	 */
	private class RedisChildWatcher extends ZKClient.ChildrenWatcher {

		private String path;

		private RedisChildWatcher(String path) {
			this.path = path;
		}

		@Override
		public void nodeRemoved(String node) {
			try {
				init(path);
			} catch (Exception e) {
				LOGGER.error("init(path): {path:" + path + "}", e);
			}
		}

		@Override
		public void nodeAdded(String node) {
			watchNode(path, node);
		}
	}

	/**
	 * 服务节点状态监控器
	 */
	private class RedisStringValueWatcher implements
			ZKClient.StringValueWatcher {

		private String path;

		private RedisStringValueWatcher(String path) {
			this.path = path;
		}

		@Override
		public void valueChanged(String l) {
			try {
				init(path);
			} catch (Exception e) {
				LOGGER.error("init(path): {path:" + path + "}", e);
			}
		}
	}

	private void watchDir(String path) {
		RedisChildWatcher cw = new RedisChildWatcher(path);
		ZKClient.getInstance().watchChildren(path, cw);
	}

	private void watchNode(String path, String nodeName) {
		if (path == null || nodeName == null) {
			return;
		}
		ServerInfo s = ConfigUtil.transTo(nodeName);
		if (s == null) {
			return;
		}
		RedisStringValueWatcher sw = new RedisStringValueWatcher(path);
		ZKClient.getInstance().watchStrValueNode(path + "/" + nodeName, sw);
	}

	/**
	 * 获取Jedis连接池
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private static JedisPool getJedisPool(String ip, int port)
			throws JedisConnectionException, JedisClientException {
		if (StringUtils.isBlank(ip) || port <= 0) {
			Map<String, Object> para = new HashMap<String, Object>();
			para.put("error", "ip is not allow null and port must >0");
			para.put("callFunc", "JedisExecutor.getJedisPool");
			para.put("ip", ip);
			para.put("port", port);
			LOGGER.warn("param invalid", para, "");
			throw new JedisClientException("INIT_ERROR", "ip is not allow null and port must >0, param:" + para);
		}
		return new JedisPool(config, ip, port);
	}

	/**
	 * 查询是否存在
	 * 
	 * @param
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<Boolean> exists(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Boolean>(ExtOperType.QUERY) {
			@Override
			Boolean exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.exists(keystr);
			}
		}, null, new String[0]);
	}

	public RedisReturn<Boolean> hexists(JedisKeyNS key, Serializable id,
			String field) throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Boolean>(ExtOperType.QUERY) {
			@Override
			Boolean exec(Jedis jedis, String keystr, Object field,
					String... values) {
				return jedis.hexists(keystr, (String) field);
			}
		}, field);
	}

	/**
	 * 往指定Redis节点（ip port） 执行 expire
	 * 
	 * @param key
	 * @param id
	 * @param seconds
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> expire(JedisKeyNS key, Serializable id, int seconds)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.EXPIRE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object seconds,
					String... values) {
				return jedis.expire(keystr, (Integer) seconds);
			}
		}, seconds, new String[0]);
	}

	/**
	 * 
	 * <Description>this is a method</Description>
	 * 
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值。注意：该方法将调用value的toString()方法作为存储的实际值
	 * @return 返回该键所对应值的长度
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> append(JedisKeyNS key, Serializable id, Object value)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.append(keystr, values[0]);
			}
		}, null, value.toString());
	}

	/**
	 * 往指定Redis节点（ip port） 执行 set
	 * 
	 * @param key
	 * @param id
	 * @param value
	 * 
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> set(JedisKeyNS key, Serializable id, Object value)
			throws JedisConnectionException, JedisClientException {

		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			public String exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.set(keystr, values[0]);
			}
		}, null, value.toString());

	}
	
	/**
	 * set if not exists
	 * @param key
	 * @param id
	 * @param value
	 * @return
	 * @throws JedisConnectionException
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> setIfAbsent(JedisKeyNS key, Serializable id, Object value)
			throws JedisConnectionException, JedisClientException {

		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			public Long exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.setnx(keystr, values[0]);
			}
		}, null, value.toString());

	}

	/**
	 * 从指定Redis节点（ip port） 执行 get
	 * 
	 * 
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> get(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.QUERY) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.get(keystr);
			}
		}, null, new String[0]);
	}

	public RedisReturn<String> getSet(JedisKeyNS key, Serializable id,
			Object newValue) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.getSet(keystr, (String) middleKey);
			}
		}, newValue, new String[0]);
	}

	/**
	 * 从指定Redis节点执行 del
	 * 
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> del(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.del(keystr);
			}
		}, null, new String[0]);
	}

	/**
	 * 随机从任一可用Redis节点执行 hset
	 * 
	 * @param key
	 * @param id
	 * @param field
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> hset(JedisKeyNS key, Serializable id, String field,
			Object value) throws JedisConnectionException, JedisClientException {

		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.hset(keystr, (String) middleKey, values[0]);
			}
		}, field, value.toString());

	}

	public RedisReturn<String> hget(JedisKeyNS key, Serializable id, String field)
			throws JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.QUERY) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.hget(keystr, (String) middleKey);
			}
		}, field, new String[0]);
	}

	public RedisReturn<Map<String, String>> hgetAll(JedisKeyNS key,
			Serializable id) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<Map<String, String>>(
				ExtOperType.QUERY) {
			@Override
			Map<String, String> exec(Jedis jedis, String keystr,
					Object middleKey, String... values) {
				return jedis.hgetAll(keystr);
			}
		}, null);
	}

	public RedisReturn<Long> hdel(JedisKeyNS key, Serializable id,
			String... fields) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... fields) {
				return jedis.hdel(keystr, fields);
			}
		}, null, fields);
	}

	public RedisReturn<String> hmset(JedisKeyNS key, Serializable id,
			Map<String, String> map) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.hmset(keystr, (HashMap<String, String>) middleKey);
			}
		}, map, new String[0]);
	}

	public RedisReturn<List<String>> hmget(JedisKeyNS key, Serializable id,
			String... fields) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<List<String>>(ExtOperType.QUERY) {
			@Override
			List<String> exec(Jedis jedis, String keystr, Object middleKey,
					String... fields) {
				return jedis.hmget(keystr, fields);
			}
		}, null, fields);
	}

	/**
	 * 将名称为key的hash中field的value增加指定value值的数量
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> hincrby(JedisKeyNS key, Serializable id, String field,long value)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					  String... values) {
				return jedis.hincrBy(keystr, (String) middleKey,Long.valueOf(values[0]));
			}
		}, field, value+"");
	}

	public RedisReturn<Long> sadd(JedisKeyNS key, Serializable id,
			String... members) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... members) {
				return jedis.sadd(keystr, members);
			}
		}, null, members);
	}

	public RedisReturn<Set<String>> smembers(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Set<String>>(ExtOperType.QUERY) {
			@Override
			Set<String> exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.smembers(keystr);
			}
		}, null);
	}

	public RedisReturn<Boolean> sismember(JedisKeyNS key, Serializable id,
			String value) throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Boolean>(ExtOperType.QUERY) {
			@Override
			Boolean exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.sismember(keystr, values[0]);
			}
		}, null, value);
	}

	public RedisReturn<Long> scard(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.QUERY) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.scard(keystr);
			}
		}, null);
	}

	public RedisReturn<String> srandmember(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.QUERY) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					String... values) {
				return jedis.srandmember(keystr);
			}
		}, null);
	}

	public RedisReturn<Long> srem(JedisKeyNS key, Serializable id,
			String... members) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object score,
					String... values) {
				return jedis.srem(keystr, values);
			}
		}, null, members);
	}

	public RedisReturn<List<String>> sort(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<List<String>>(ExtOperType.QUERY) {
			@Override
			List<String> exec(Jedis jedis, String keystr, Object score,
					String... values) {
				return jedis.sort(keystr);
			}
		}, null);
	}

	public RedisReturn<List<String>> sort(JedisKeyNS key, Serializable id,
			SortingParams params) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<List<String>>(ExtOperType.QUERY) {
			@Override
			List<String> exec(Jedis jedis, String keystr, Object params,
					String... values) {
				return jedis.sort(keystr, (SortingParams) params);
			}
		}, params);
	}

	public RedisReturn<Long> zadd(JedisKeyNS key, Serializable id, double score,
			Object value) throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object score,
					String... values) {
				return jedis.zadd(keystr, (Double) score, values[0]);
			}
		}, score, value.toString());
	}

//	public RedisReturn<Long> zadd(JedisKeyNS key, Serializable id,
//			Map<Double, String> scoreMembers) throws JedisConnectionException,
//			JedisClientException {
//		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
//			@Override
//			Long exec(Jedis jedis, String keystr, Object scoreMembers,
//					String... values) {
//				return jedis.zadd(keystr, (Map<Double, String>) scoreMembers);
//			}
//		}, scoreMembers);
//	}

	public RedisReturn<Long> zrem(JedisKeyNS key, Serializable id,
			String... members) throws JedisConnectionException,
			JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... members) {
				return jedis.zrem(keystr, members);
			}
		}, null, members);
	}

	public RedisReturn<Long> decr(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... members) {
				return jedis.decr(keystr);
			}
		}, null);
	}

	public RedisReturn<Long> decrBy(JedisKeyNS key, Serializable id, int byvalue)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object byvalue,
					String... members) {
				return jedis.decrBy(keystr, (Long) byvalue);
			}
		}, Long.parseLong(byvalue + ""));
	}

	public RedisReturn<Long> incr(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
					String... members) {
				return jedis.incr(keystr);
			}
		}, null);
	}

	public RedisReturn<Long> incrBy(JedisKeyNS key, Serializable id, int byvalue)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object byvalue,
					String... members) {
				return jedis.incrBy(keystr, (Long) byvalue);
			}
		}, Long.parseLong(byvalue + ""));
	}


	public RedisReturn<Long> rpush(JedisKeyNS key, Serializable id, Object value)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object byvalue,
					  String... values) {
				return jedis.rpush(keystr, values[0]);
			}
		}, null,value.toString());
	}

	public RedisReturn<Long> lpush(JedisKeyNS key, Serializable id, Object value)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object byvalue,
					  String... values) {
				return jedis.lpush(keystr, values[0]);
			}
		}, null,value.toString());
	}

	/**
	 * 返回名称为key的list的长度
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> llen(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.QUERY) {
			@Override
			Long exec(Jedis jedis, String keystr, Object middleKey,
						String... values) {
				return jedis.llen(keystr);
			}
		}, null, new String[0]);
	}

	/**
	 * 返回名称为key的list中start至end之间的元素（下标从0开始）
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<List<String>> lrange(JedisKeyNS key, Serializable id, final long start, final long end)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<List<String>>(ExtOperType.QUERY) {
			@Override
			List<String> exec(Jedis jedis, String keystr, Object middleKey,
					  String... values) {
				return jedis.lrange(keystr,Long.valueOf(values[0]),Long.valueOf(values[1]));
			}
		}, null, new String[]{start+"",end+""});
	}

	/**
	 * 截断名称为key的list，保留start至end之间的元素
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> ltrim(JedisKeyNS key, Serializable id, final long start, final long end)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
							  String... values) {
				return jedis.ltrim(keystr,Long.valueOf(values[0]),Long.valueOf(values[1]));
			}
		}, null, new String[]{start+"",end+""});
	}

	/**
	 * 返回名称为key的list中index位置的元素
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> lindex(JedisKeyNS key, Serializable id,long index)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.QUERY) {
			@Override
			String exec(Jedis jedis, String keystr, Object index,
					  String... values) {
				return jedis.lindex(keystr,(Long)index);
			}
		}, index);
	}

	/**
	 * 给名称为key的list中index位置的元素赋值为value
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> lset(JedisKeyNS key, Serializable id,long index,Object value)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			String exec(Jedis jedis, String keystr, Object index,
					  String... values) {
				return jedis.lset(keystr,(Long)index,values[0]);
			}
		}, index, value.toString());
	}

	/**
	 * 删除count个名称为key的list中值为value的元素。<br>
	 * count为0，删除所有值为value的元素，<br>
	 * count>0从头至尾删除count个值为value的元素，<br>
	 * count小于0从尾到头删除count个值为value的元素。
	 *
	 * @param key
	 * @param id
	 * @return 成功删除的元素个数
	 * @throws JedisClientException
	 */
	public RedisReturn<Long> lrem(JedisKeyNS key, Serializable id,long count,Object value)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<Long>(ExtOperType.CHANGE) {
			@Override
			Long exec(Jedis jedis, String keystr, Object count,
					  String... values) {
				return jedis.lrem(keystr,(Long)count,values[0]);
			}
		}, count, value.toString());
	}

	/**
	 * 返回并删除名称为key的list中的首元素
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> lpop(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					  String... values) {
				return jedis.lpop(keystr);
			}
		}, null, new String[0]);
	}

	/**
	 * 返回并删除名称为key的list中的尾元素
	 *
	 *
	 * @param key
	 * @param id
	 * @return
	 * @throws JedisClientException
	 */
	public RedisReturn<String> rpop(JedisKeyNS key, Serializable id)
			throws JedisConnectionException, JedisClientException {
		return extExec(key, id, new ExtOper<String>(ExtOperType.CHANGE) {
			@Override
			String exec(Jedis jedis, String keystr, Object middleKey,
					  String... values) {
				return jedis.rpop(keystr);
			}
		}, null, new String[0]);
	}


	/**
	 * <Description>this is a method</Description>
	 * 
	 * @param key
	 *            JedisKeyNS对象
	 * @param id
	 *            键的唯一标识
	 * @return 返回由key.buildKey()+JedisKeyNS.KEY_SPLITER+id.toString()形式的字符串
	 */
	private static String buildKey(JedisKeyNS key, Serializable id) {
		return key.getNameSpace() + CacheNameSpace.KEY_SPLITER + id.toString();
	}

	/**
	 * 
	 * <Description>对于那些在本类中暂未实现的方法，可以由此方法进行扩展</Description>
	 * 
	 * @param <T>
	 *            范型，定义返回类型
	 * @param key
	 *            键类型
	 * @param id
	 *            键的唯一标识值
	 * @param oper
	 *            ExtOper<T>的子类对象
	 * @param middleKey
	 *            只有hset、zadd这种类型，需要有一个中间域标识的数据类型才需要传值，其它可以传null
	 * @return 返回指定类型的对象
	 */
	private <T> T extExec(Jedis jedis, JedisKeyNS key, Serializable id,
			ExtOper<T> oper, Object middleKey, String... values)
			throws JedisConnectionException, JedisDataException,
			JedisClientException {

		if (jedis == null || key == null || id == null) {
			throw new JedisClientException(
					"EXEC_ERROR",
					"jedis instanse,key,id must not null, param(jedis:"
							+ jedis
							+ ",JedisKeyNS:"
							+ key
							+ ",id:"
							+ id
							+ ",middleKey:"
							+ middleKey
							+ (ExtOperType.QUERY.equals(oper.getExOperType()) ? ""
									: ",values:"
											+ (values == null ? null : Arrays
													.asList(values))) + ")");
		}

		String keystr = buildKey(key, id);

		T rs = oper.exec(jedis, keystr, middleKey, values);
		if (key.getExpire() > 0 && oper.getExOperType() == ExtOperType.CHANGE) {
			jedis.expire(keystr, key.getExpire());
		}

		return rs;

	}

	public boolean subscribeExec(final JedisPubSub jedisPubSub, final String... channels){
		boolean hasPool = !(jedisPools.size() == 0);
		if (hasPool){
			JedisPool pool = null;
			int index = 0;
			for (; index < JedisKeyNS.RETRY_TIMES; index++) {
				Jedis jedis = null;
				int hit = random.nextInt(jedisPools.size());
				pool = jedisPools.get(hit);
				LOGGER.debug("subscribeExec from jedisServer["+pool.getHost()+":"+pool.getPort()
						+"]'s channel["+Arrays.asList(channels)+"]");				
				try {
					jedis = pool.getResource();
					jedis.subscribe(jedisPubSub, channels);;
					return true;
				} catch (JedisConnectionException jce) {

					if (jedis == null || !jedis.isConnected()) {
						synchronized (this) {
							if (jedis == null || !jedis.isConnected()) {
								JedisPool deadPool = pool;
								pool = retryConnect(pool, jedis, index);
								try {
									jedisPools.remove(deadPool);
									jedisPools.add(pool);
								} catch (Exception ex) {
									LOGGER.error(ex.getMessage(), ex);
								}
							}
						}
					}
				} catch (JedisDataException jde) {
					throw new JedisClientException(
							"EXEC_ERROR", "subscribeExec channel:"+Arrays.asList(channels));
				} finally {
					pool.returnResourceObject(jedis);
				}				
			}
			return false;
		}else{
			LOGGER.warn(
					"all redis has bean down",
					"can not subscribe channel:"+Arrays
					.asList(channels));			
			return hasPool;
		}
	}
	
	public int publish(final String channel, final String message){
		boolean hasPool = !(jedisPools.size() == 0);
		if (hasPool){
			long singleResult = 0;
			JedisPool pool = null;
			int index = 0;
			//for (; index < JedisKeyNS.RETRY_TIMES; index++) {
				Iterator<JedisPool> itr = jedisPools.iterator();
					int jedisIndex = 0;
					while (itr.hasNext()) {
						pool = itr.next();
						jedisIndex++;
						Jedis jedis  = pool.getResource();
						try{
							singleResult = jedis.publish(channel, message);
							//LOGGER.info(singleResult+" subscriber accept the "+channel+"'s message.");
							//publishServerCount++;
							LOGGER.debug(singleResult+" Subscribers accept Publish message["+message+"] to jedisServer["+pool.getHost()+":"+pool.getPort()
									+"]'s channel["+channel+"]");
							if (singleResult>0) break;
							
						} catch (JedisConnectionException jce) {
							if (jedis == null || !jedis.isConnected()) {
								LOGGER.error(jce.getMessage(), jce);
							}
						} catch (JedisDataException jde) {
							LOGGER.error(jde.getMessage(), jde);
						} finally {
							pool.returnResourceObject(jedis);
						}
					}
				//if (publishServerCount>0) break;
			//}
			return (int)singleResult;
		}else{
			LOGGER.warn(
					"all redis has bean down",
					"can not publish channel:"+channel);			
			return 0;
		}		
	}
	
	/**
	 * <Description>对于那些在本类中暂未实现的方法，可以由此方法进行扩展</Description>
	 * 
	 * @param key
	 *            键类型
	 * @param id
	 *            键的唯一标识值
	 * @param oper
	 *            ExtOper<T>的子类对象
	 * @param middleKey
	 *            只有hset、zadd这种类型，需要有一个中间域标识的数据类型才需要传值，其它可以传null
	 * @param values
	 * @param <T>
	 *            范型，定义返回类型
	 * @return 返回指定类型的对象
	 * @throws JedisClientException
	 */
	private <T> RedisReturn<T> extExec(JedisKeyNS key, Serializable id,
			ExtOper<T> oper, Object middleKey, String... values)
			throws JedisConnectionException, JedisClientException {

		precheck(key, id);

		boolean hasPool = (jedisPools.size() == 0);
		RedisReturn<T> redisReturn = new RedisReturn<T>(hasPool);
		if (hasPool) {
			LOGGER.warn(
					"all redis has bean down",
					"JedisKeyNS:"
							+ key
							+ ",id:"
							+ id
							+ ",middleKey:"
							+ middleKey
							+ (ExtOperType.QUERY.equals(oper.getExOperType()) ? ""
									: ",values:"
											+ (values == null ? null : Arrays
													.asList(values))), "");
			return redisReturn;
		}

		T result = null;
		if (ExtOperType.CHANGE.equals(oper.getExOperType())
				|| ExtOperType.EXPIRE.equals(oper.getExOperType())) {
			Iterator<JedisPool> itr = jedisPools.iterator();
			while (itr.hasNext()) {
				result = execByPool(itr.next(), key, id, oper, middleKey,
						values);
				if (result != null) {
					redisReturn.setOperRs(result);
				}
			}
		} else {
			int hit = random.nextInt(jedisPools.size());

			result = execByPool(jedisPools.get(hit), key, id, oper, middleKey,
					values);
			redisReturn.setOperRs(result);
		}

		return redisReturn;
	}

	private <T> T execByPool(JedisPool pool, JedisKeyNS key, Serializable id,
			ExtOper<T> oper, Object middleKey, String... values)
			throws JedisConnectionException, JedisClientException {
		long accessTime = System.nanoTime();
		Jedis jedis = null;
		int index = 0;
		for (; index < JedisKeyNS.RETRY_TIMES; index++) {
			try {
				LOGGER.debug(LOGGER.isDebugEnabled() ? "extExec param(host:"
						+ pool.getHost()
						+ ",port:"
						+ pool.getPort()
						+ ",JedisKeyNS:"
						+ key
						+ ",id:"
						+ id
						+ ",middleKey:"
						+ middleKey
						+ ",keyType:"
						+ key.getKeyType()
						+ ",operType:"
						+ oper.getExOperType()
						+ (ExtOperType.QUERY.equals(oper.getExOperType()) ? ""
								: ",values:"
										+ (values == null ? null : Arrays
												.asList(values))) + ")" : null);
				jedis = pool.getResource();
				T result = extExec(jedis, key, id, oper, middleKey, values);

				return result;
			} catch (JedisConnectionException jce) {

				// 这个地方需要重点测试一下
				if (jedis == null || !jedis.isConnected()) {
					synchronized (this) {
						if (jedis == null || !jedis.isConnected()) {
							JedisPool deadPool = pool;
							pool = retryConnect(pool, jedis, index);
							try {
								jedisPools.remove(deadPool);
								jedisPools.add(pool);
							} catch (Exception ex) {
								LOGGER.error(ex.getMessage(), ex);
							}
						}
					}
				}
			} catch (JedisDataException jde) {
				throw new JedisClientException(
						"EXEC_ERROR", "param(host:"
								+ pool.getHost()
								+ ",port:"
								+ pool.getPort()
								+ ",JedisKeyNS:"
								+ key
								+ ",id:"
								+ id
								+ ",middleKey:"
								+ middleKey
								+ ",values:"
								+ (values == null ? null
										: Arrays.asList(values)) + ")");
			} finally {
				pool.returnResourceObject(jedis);
				TimeLog.log(
						key,
						id,
						accessTime,
						"keyType:"
								+ key.getKeyType()
								+ ",operType:"
								+ oper.getExOperType()
								+ ","
								+ pool.getHost() + ":" + pool.getPort());
			}
		}

//		if (index >= JedisKeyNS.RETRY_TIMES) {
//
//			try {
//				ZKClient.getInstance().setData(
//						JedisKeyNS.GROUP_NODE + "/r/" + pool.getHost() + ":"
//								+ pool.getPort(), "false".getBytes());
//				LOGGER.info("suceess set false to Zookeeper. " + pool.getHost()
//						+ " " + pool.getPort(), null, null);
//			} catch (Exception e1) {
//				Map<String, Object> para = new HashMap<String, Object>();
//				para.put("nodePath",
//						JedisKeyNS.GROUP_NODE + "/r/" + pool.getHost() + ":"
//								+ pool.getPort());
//				para.put("content", "false".getBytes());
//				LOGGER.error(e1.getMessage(), e1);
//			}
//		}

		return null;
	}

	private static JedisPool retryConnect(JedisPool pool, Jedis currJedis,
			int index) throws JedisClientException {
		try {
			Thread.sleep(JedisKeyNS.SLEEP_TIME);
			pool.returnResourceObject(currJedis);
			pool.destroy();
		} catch (Exception e1) {
			LOGGER.error("sleep in getJedis method error", e1);
		}

		LOGGER.debug("retry create jedis instanse. -> " + index);

		try {
			pool = getJedisPool(pool.getHost(), pool.getPort());
		} catch (JedisConnectionException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		return pool;
	}

	private static void precheck(JedisKeyNS key, Serializable id)
			throws JedisClientException {
		if (key == null || id == null) {
			throw new JedisClientException("400", "key or id is not allow null");
		}
	}

}
