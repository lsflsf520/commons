package com.yisi.stiku.cache.eh;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.cache.constant.CacheNameSpace;
import com.yisi.stiku.cache.constant.CacheConstant;
import com.yisi.stiku.common.utils.BeanUtils;
import com.yisi.stiku.conf.BaseConfig;

public class EhCacheTool {
	
	private final static Logger LOG = LoggerFactory.getLogger(EhCacheTool.class);

	private static String cacheConfigFile = BaseConfig.getValue("ehcache.config.file", BaseConfig.getPath("ehcache.xml"));
	
	private final static EhCacheTool CACHE_INSTANCE = new EhCacheTool();
	
//	private final static CacheManager MANAGER = CacheManager.create("D:/repos/git/stiku_all/stiku-parent/common-parent/common-cache/src/test/resources/ehcache.xml");
	
	private final static CacheManager MANAGER ;
	
	static{
		
		if(cacheConfigFile.startsWith("file:/")){
			//windows下的路径可能是 file:/D:/xx/xx/ehcache.xml的格式；linux下的路径可能是 file:/xx/xx/xx/ehcache.xml 的格式
			cacheConfigFile = cacheConfigFile.replace("file:/", System.getProperty("os.name").contains("Windows") ? "" : "/");
		}
		
		LOG.debug(LOG.isDebugEnabled() ? "init ehcache with config file '" + cacheConfigFile + "'"  : "");
		MANAGER = CacheManager.create(cacheConfigFile);
	}
	
	
	
	private final Object CACHE_LOCK = new Object();
	
	private EhCacheTool(){
		if(StringUtils.isBlank(cacheConfigFile)){
			throw new IllegalStateException("ehCache config file 'ehcache.xml' not found");
		}
		
	}
	
	public static EhCacheTool getInstance(){
		return CACHE_INSTANCE;
	}
	
	/**
	 * 
	 * @param namespace
	 * @param value
	 */
	public static void put(CacheNameSpace namespace, Object value){
		put(namespace, "", value);
	}
	
	/**
	 * 
	 * @param namespace
	 * @return
	 */
	public static <T> T getValue(CacheNameSpace namespace){
		return getValue(namespace, "");
	}
	
	public static boolean remove(CacheNameSpace namespace){
		return remove(namespace, "");
	}
	
	public static void put(CacheNameSpace namespace, Serializable key, Object value){
		if(key == null){
			throw new IllegalArgumentException("parameter key for ehcache cannot be null");
		}
		String cacheName = CacheConstant.DEFAULT_CACHE_NAME;
		if(namespace != null && StringUtils.isNotBlank(namespace.getNameSpace())){
			cacheName = namespace.getNameSpace();
		}
		Cache cache = getInstance().getCache(cacheName);
		
		if(cache == null){
			LOG.warn("cache for namespace '" + namespace.getNameSpace() + "' not exists");
			return;
		}
		
		Element elem = null;
		if(namespace.getExpire() > 0){
			elem = new Element(key, value, false, namespace.getExpire(), namespace.getExpire());
		}else{
			elem = new Element(key, value);
		}
		cache.put(elem);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getValue(CacheNameSpace namespace, Serializable key){
		if(key == null){
			throw new IllegalArgumentException("parameter key for ehcache cannot be null");
		}
		String cacheName = CacheConstant.DEFAULT_CACHE_NAME;
		if(namespace != null && StringUtils.isNotBlank(namespace.getNameSpace())){
			cacheName = namespace.getNameSpace();
		}
		Cache cache = getInstance().getCache(cacheName);
		
		if(cache == null){
			LOG.warn("cache for namespace '" + namespace.getNameSpace() + "' not exists");
			return null;
		}
		
		Element elem = cache.get(key);
		try{
		    return elem == null || elem.getObjectValue() == null ? null : (T)BeanUtils.deepClone(elem.getObjectValue());
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
	
	public static boolean remove(CacheNameSpace namespace, Serializable key){
		if(key == null){
			throw new IllegalArgumentException("parameter key for ehcache cannot be null");
		}
		
		String cacheName = CacheConstant.DEFAULT_CACHE_NAME;
		if(namespace != null && StringUtils.isNotBlank(namespace.getNameSpace())){
			cacheName = namespace.getNameSpace();
		}
		Cache cache = getInstance().getCache(cacheName);
		
		if(cache == null){
			LOG.warn("cache for namespace '" + namespace.getNameSpace() + "' not exists");
			return false;
		}
		
		return cache.remove(key);
	}
	
	public static void removeAll(CacheNameSpace namespace){
		String cacheName = CacheConstant.DEFAULT_CACHE_NAME;
		if(namespace != null && StringUtils.isNotBlank(namespace.getNameSpace())){
			cacheName = namespace.getNameSpace();
		}
		Cache cache = getInstance().getCache(cacheName);
		
		if(cache == null){
			LOG.warn("cache for namespace '" + namespace.getNameSpace() + "' not exists");
			return;
		}
		
		cache.removeAll();
	}
	
	/**
	 * 从应用中移除MANAGER实例
	 */
	public void destroy(){
		MANAGER.shutdown();
	}
	
	private Cache getCache(String cacheName){
		Cache cache = MANAGER.getCache(cacheName);
		if(cache == null){
			synchronized (CACHE_LOCK) {
				cache = MANAGER.getCache(cacheName);
				if(cache == null){
					MANAGER.addCache(cacheName);
					cache = MANAGER.getCache(cacheName);
				}
			}
		}
		
		return cache;
	}
}
