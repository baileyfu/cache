package com.lz.components.cache.local.shard;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.ShardCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.local.EHCache;
import com.lz.components.common.beanutil.SyncBeanCreater;
import com.lz.components.common.exception.LzRuntimeException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class EHShardCache extends EHCache implements ShardCache<Object, Object>, SyncBeanCreater {
	private Map<String, net.sf.ehcache.Cache> cacheMap;

	public EHShardCache(String defaultShardName) throws LzRuntimeException {
		super(defaultShardName);
		cacheMap = new HashMap<>();
		cacheMap.put(defaultShardName, super.cache);
	}

	public EHShardCache(String configurationFileName, String defaultShardName) throws LzRuntimeException {
		super(configurationFileName, defaultShardName);
		cacheMap = new HashMap<>();
		cacheMap.put(defaultShardName, super.cache);
	}

	@Override
	public void put(String shardName, Object key, Object value) throws LzRuntimeException {
		put(shardName, key, value, 0L, null);
	}

	@Override
	public void put(String shardName, Object key, Object value, long expiring, TimeUnit timeUnit)throws LzRuntimeException {
		putCount++;
		Cache cache = takeCache(shardName);
		Element element = null;
		if (expiring > 0L) {
			int expiringSeconds = timeUnit.toSeconds(expiring);
			element = new Element(key, value, expiringSeconds, expiringSeconds);
		} else {
			element = new Element(key, value);
		}
		try {
			cache.remove(key);
			cache.put(element);
		} catch (Exception e) {
			throw new LzRuntimeException(e);
		}
	}

	@Override
	public void remove(String shardName, Object key) throws LzRuntimeException {
		removeCount++;
		takeCache(shardName).remove(key);
	}

	@Override
	public Object get(String shardName, Object key) throws LzRuntimeException {
		getCount++;
		Element element = takeCache(shardName).get(key);
		if (element == null) {
			return null;
		}
		hitCount++;
		return element.getObjectValue();
	}

	@Override
	public boolean exists(String shardName, Object key) {
		return takeCache(shardName).isKeyInCache(key);
	}

	@Override
	public JSONObject size(String shardName) {
		JSONObject size = new JSONObject();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, takeCache(shardName).getSize());
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	public void clear(String shardName) throws LzRuntimeException {
		takeCache(shardName).removeAll();
	}

	private net.sf.ehcache.Cache takeCache(String shardName) {
		return syncCreate(() -> cacheMap.get(shardName), () ->cacheMap.put(shardName, cacheManager.getCache(shardName)));
	}
}
