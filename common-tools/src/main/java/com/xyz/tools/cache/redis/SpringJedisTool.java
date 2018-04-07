package com.xyz.tools.cache.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.xyz.tools.cache.constant.CacheNameSpace;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.LogUtils;

@SuppressWarnings("all")
public class SpringJedisTool implements Cache {

	private RedisTemplate<String, Object> redisTemplate;
	private String name;

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String buildKey(CacheNameSpace namespace, Serializable key) {
		return namespace.getNameSpace() + (key == null ? "" : "_" + key);
	}

	// =============================common============================
	/**
	 * 指定缓存失效时间
	 * 
	 * @param key
	 *            键
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean expire(CacheNameSpace namespace, Serializable key) {
		try {
			if (namespace.getExpire() > 0) {
				redisTemplate.expire(buildKey(namespace, key), namespace.getExpire(), TimeUnit.SECONDS);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @param seconds
	 * @return
	 */
	public boolean expire(CacheNameSpace namespace, Serializable key, int seconds) {
		try {
			int expireSeconds = seconds;
			if (expireSeconds <= 0) {
				expireSeconds = namespace.getExpire();
			}
			if (expireSeconds > 0) {
				redisTemplate.expire(buildKey(namespace, key), expireSeconds, TimeUnit.SECONDS);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 根据key 获取过期时间
	 * 
	 * @param key
	 *            键 不能为null
	 * @return 时间(秒) 返回0代表为永久有效
	 */
	public long getExpire(CacheNameSpace namespace, String key) {
		return redisTemplate.getExpire(buildKey(namespace, key), TimeUnit.SECONDS);
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 *            键
	 * @return true 存在 false不存在
	 */
	public boolean hasKey(CacheNameSpace namespace, Serializable key) {
		try {
			return redisTemplate.hasKey(buildKey(namespace, key));
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 删除缓存
	 * 
	 * @param key
	 *            可以传一个值 或多个
	 */
	@SuppressWarnings("unchecked")
	public void del(CacheNameSpace namespace, Serializable... keys) {
		if (keys != null && keys.length > 0) {
			if (keys.length == 1) {
				redisTemplate.delete(buildKey(namespace, keys[0]));
			} else {
				List<String> realKeys = new ArrayList<>();
				for (Serializable key : keys) {
					if (key != null) {
						realKeys.add(buildKey(namespace, key));
					}
				}
				redisTemplate.delete(realKeys);
			}
		}
	}

	// ============================String=============================
	/**
	 * 普通缓存获取
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	public <T> T get(CacheNameSpace namespace, Serializable key) {
		return key == null ? null : (T) redisTemplate.opsForValue().get(buildKey(namespace, key));
	}

	/**
	 * 普通缓存放入
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return true成功 false失败
	 */
	public boolean set(CacheNameSpace namespace, Serializable key, Object value) {
		try {
			if (namespace.getExpire() > 0) {
				redisTemplate.opsForValue().set(buildKey(namespace, key), value, namespace.getExpire(),
						TimeUnit.SECONDS);
			} else {
				redisTemplate.opsForValue().set(buildKey(namespace, key), value);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @param value
	 * @return
	 */
	public <T> T getset(CacheNameSpace namespace, Serializable key, Object value) {
		try {
			Object retVal = redisTemplate.opsForValue().getAndSet(buildKey(namespace, key), value);
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return retVal == null ? null : (T) retVal;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setIfAbsent(CacheNameSpace namespace, Serializable key, Object value) {
		try {
			Boolean result = redisTemplate.opsForValue().setIfAbsent(buildKey(namespace, key), value);
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return result == null ? false : result;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 递增
	 * 
	 * @param key
	 *            键
	 * @param by
	 *            要增加几(大于0)
	 * @return
	 */
	public long incr(CacheNameSpace namespace, Serializable key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递增因子必须大于0");
		}
		return redisTemplate.opsForValue().increment(buildKey(namespace, key), delta);
	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @return
	 */
	public long incr(CacheNameSpace namespace, Serializable key) {
		return this.incr(namespace, key, 1);
	}

	/**
	 * 递减
	 * 
	 * @param key
	 *            键
	 * @param by
	 *            要减少几(小于0)
	 * @return
	 */
	public long decr(CacheNameSpace namespace, Serializable key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递减因子必须大于0");
		}
		return redisTemplate.opsForValue().increment(buildKey(namespace, key), -delta);
	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @return
	 */
	public long decr(CacheNameSpace namespace, Serializable key) {
		return this.decr(namespace, key, 1);
	}

	// ================================Map=================================
	/**
	 * HashGet
	 * 
	 * @param key
	 *            键 不能为null
	 * @param item
	 *            项 不能为null
	 * @return 值
	 */
	public <T> T hget(CacheNameSpace namespace, Serializable key, String item) {
		return (T) redisTemplate.opsForHash().get(buildKey(namespace, key), item);
	}

	/**
	 * 获取hashKey对应的所有键值
	 * 
	 * @param key
	 *            键
	 * @return 对应的多个键值
	 */
	public Map<Object, Object> hmget(CacheNameSpace namespace, Serializable key) {
		return redisTemplate.opsForHash().entries(buildKey(namespace, key));
	}

	/**
	 * HashSet
	 * 
	 * @param key
	 *            键
	 * @param map
	 *            对应多个键值
	 * @return true 成功 false 失败
	 */
	public boolean hmset(CacheNameSpace namespace, Serializable key, Map<String, Object> map) {
		try {
			redisTemplate.opsForHash().putAll(buildKey(namespace, key), map);
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * HashSet 并设置时间
	 * 
	 * @param key
	 *            键
	 * @param map
	 *            对应多个键值
	 * @param time
	 *            时间(秒)
	 * @return true成功 false失败
	 * 
	 *         public boolean hmset(String key, Map<String, Object> map, long time)
	 *         { try { redisTemplate.opsForHash().putAll(key, map); if (time > 0) {
	 *         expire(key, time); } return true; } catch (Exception e) {
	 *         LogUtils.error(e.getMessage(), e); return false; } }
	 */

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param value
	 *            值
	 * @return true 成功 false失败
	 */
	public boolean hset(CacheNameSpace namespace, Serializable key, String item, Object value) {
		try {
			redisTemplate.opsForHash().put(buildKey(namespace, key), item, value);
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @param item
	 * @param value
	 * @return
	 */
	public boolean hsetIfAbsent(CacheNameSpace namespace, Serializable key, String item, Object value) {
		try {
			Boolean result = redisTemplate.opsForHash().putIfAbsent(buildKey(namespace, key), item, value);
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return result == null ? false : result;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
	 * @return true 成功 false失败 public boolean hset(String key, String item, Object
	 *         value, long time) { try { redisTemplate.opsForHash().put(key, item,
	 *         value); if (time > 0) { expire(key, time); } return true; } catch
	 *         (Exception e) { LogUtils.error(e.getMessage(), e); return false; } }
	 */

	/**
	 * 删除hash表中的值
	 * 
	 * @param key
	 *            键 不能为null
	 * @param item
	 *            项 可以使多个 不能为null
	 */
	public void hdel(CacheNameSpace namespace, Serializable key, Object... item) {
		redisTemplate.opsForHash().delete(buildKey(namespace, key), item);
	}

	/**
	 * 判断hash表中是否有该项的值
	 * 
	 * @param key
	 *            键 不能为null
	 * @param item
	 *            项 不能为null
	 * @return true 存在 false不存在
	 */
	public boolean hHasKey(CacheNameSpace namespace, Serializable key, String item) {
		return redisTemplate.opsForHash().hasKey(buildKey(namespace, key), item);
	}

	/**
	 * hash递增 如果不存在,就会创建一个 并把新增后的值返回
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param by
	 *            要增加几(大于0)
	 * @return
	 */
	public double hincr(CacheNameSpace namespace, Serializable key, String item, double by) {
		return redisTemplate.opsForHash().increment(buildKey(namespace, key), item, by);
	}

	/**
	 * hash递减
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param by
	 *            要减少记(小于0)
	 * @return
	 */
	public double hdecr(CacheNameSpace namespace, Serializable key, String item, double by) {
		return redisTemplate.opsForHash().increment(buildKey(namespace, key), item, -by);
	}

	// ============================set=============================
	/**
	 * 根据key获取Set中的所有值
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Set<Object> sGet(CacheNameSpace namespace, Serializable key) {
		try {
			return redisTemplate.opsForSet().members(buildKey(namespace, key));
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 根据value从一个set中查询,是否存在
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return true 存在 false不存在
	 */
	public boolean sHasKey(CacheNameSpace namespace, Serializable key, Object value) {
		try {
			return redisTemplate.opsForSet().isMember(buildKey(namespace, key), value);
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 将数据放入set缓存
	 * 
	 * @param key
	 *            键
	 * @param values
	 *            值 可以是多个
	 * @return 成功个数
	 */
	public long sSet(CacheNameSpace namespace, Serializable key, Object... values) {
		try {
			Long count = redisTemplate.opsForSet().add(buildKey(namespace, key), values);
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return count;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * 将set数据放入缓存
	 * 
	 * @param key
	 *            键
	 * @param time
	 *            时间(秒)
	 * @param values
	 *            值 可以是多个
	 * @return 成功个数 public long sSetAndTime(String key, long time, Object... values)
	 *         { try { Long count = redisTemplate.opsForSet().add(key, values); if
	 *         (time > 0) expire(key, time); return count; } catch (Exception e) {
	 *         LogUtils.error(e.getMessage(), e); return 0; } }
	 */

	/**
	 * 获取set缓存的长度
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public long sGetSize(CacheNameSpace namespace, Serializable key) {
		try {
			return redisTemplate.opsForSet().size(buildKey(namespace, key));
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * 移除值为value的
	 * 
	 * @param key
	 *            键
	 * @param values
	 *            值 可以是多个
	 * @return 移除的个数
	 */
	public long sRemove(CacheNameSpace namespace, Serializable key, Object... values) {
		try {
			Long count = redisTemplate.opsForSet().remove(buildKey(namespace, key), values);
			return count;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return 0;
		}
	}
	// ===============================list=================================

	/**
	 * 获取list缓存的内容
	 * 
	 * @param key
	 *            键
	 * @param start
	 *            开始
	 * @param end
	 *            结束 0 到 -1代表所有值
	 * @return
	 */
	public List<Object> lGet(CacheNameSpace namespace, Serializable key, long start, long end) {
		try {
			return redisTemplate.opsForList().range(buildKey(namespace, key), start, end);
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 获取list缓存的长度
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public long lGetSize(CacheNameSpace namespace, Serializable key) {
		try {
			return redisTemplate.opsForList().size(buildKey(namespace, key));
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * 通过索引 获取list中的值
	 * 
	 * @param key
	 *            键
	 * @param index
	 *            索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
	 * @return
	 */
	public <T> T lGetIndex(CacheNameSpace namespace, Serializable key, long index) {
		try {
			return (T) redisTemplate.opsForList().index(buildKey(namespace, key), index);
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 将list放入缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean lSet(CacheNameSpace namespace, Serializable key, Object... values) {
		try {
			if (values != null && values.length > 0) {
				if (values.length == 1) {
					redisTemplate.opsForList().leftPush(buildKey(namespace, key), values[0]);
				} else {
					redisTemplate.opsForList().leftPushAll(buildKey(namespace, key), Arrays.asList(values));
				}
			}
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 
	 * @param namespace
	 * @param key
	 * @param values
	 * @return
	 */
	public boolean rSet(CacheNameSpace namespace, Serializable key, Object... values) {
		try {
			if (values != null && values.length > 0) {
				if (values.length == 1) {
					redisTemplate.opsForList().rightPush(buildKey(namespace, key), values[0]);
				} else {
					redisTemplate.opsForList().rightPushAll(buildKey(namespace, key), Arrays.asList(values));
				}
			}
			if (namespace.getExpire() > 0) {
				expire(namespace, key);
			}
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 根据索引修改list中的某条数据
	 * 
	 * @param key
	 *            键
	 * @param index
	 *            索引
	 * @param value
	 *            值
	 * @return
	 */
	public boolean lUpdateIndex(CacheNameSpace namespace, Serializable key, long index, Object value) {
		try {
			redisTemplate.opsForList().set(buildKey(namespace, key), index, value);
			return true;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 移除N个值为value
	 * 
	 * @param key
	 *            键
	 * @param count
	 *            移除多少个
	 * @param value
	 *            值
	 * @return 移除的个数
	 */
	public long lRemove(CacheNameSpace namespace, Serializable key, long count, Object value) {
		try {
			Long remove = redisTemplate.opsForList().remove(buildKey(namespace, key), count, value);
			return remove;
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return 0;
		}
	}

	@Override
	public void clear() {
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushDb();
				return "ok";
			}
		});

	}

	@Override
	public void evict(Object key) {
		final String keyf = key.toString();
		redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.del(keyf.getBytes());
			}
		});
	}

	@Override
	public ValueWrapper get(Object key) {
		final String keyf = key.toString();
		Object object = null;
		object = redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] key = keyf.getBytes();
				byte[] value = connection.get(key);
				if (value == null) {
					return null;
				}
				return toObject(value);
			}
		});
		return (object != null ? new SimpleValueWrapper(object) : null);
	}

	@Override
	public <T> T get(Object key, Class<T> arg1) {
		throw new BaseRuntimeException("NOT_SUPPORT", "不支持的操作");
	}

	@Override
	public <T> T get(Object key, Callable<T> arg1) {
		throw new BaseRuntimeException("NOT_SUPPORT", "不支持的操作");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object getNativeCache() {
		return this.redisTemplate;
	}

	@Override
	public void put(Object key, Object value) {
		final String keyf = key.toString();
		final Object valuef = value;
		final long liveTime = 86400;
		redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyb = keyf.getBytes();
				byte[] valueb = toByteArray(valuef);
				connection.set(keyb, valueb);
				if (liveTime > 0) {
					connection.expire(keyb, liveTime);
				}
				return 1L;
			}
		});
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		throw new BaseRuntimeException("NOT_SUPPORT", "不支持的操作");
	}

	private byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
		} catch (IOException ex) {
			LogUtils.error(ex.getMessage(), ex);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
		}
		return bytes;
	}

	private Object toObject(byte[] bytes) {
		Object obj = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bis);
			obj = ois.readObject();
		} catch (IOException ex) {
			LogUtils.error(ex.getMessage(), ex);
		} catch (ClassNotFoundException ex) {
			LogUtils.error(ex.getMessage(), ex);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					LogUtils.error(e.getMessage(), e);
				}
			}
		}
		return obj;
	}

}
