package com.lz.components.cache.local.map.shard;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.ShardCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.local.map.LRUMapCache;
import com.lz.components.common.beanutil.SyncBeanCreater;
import com.lz.components.common.exception.LzRuntimeException;

public class LRUMapShardCache extends LRUMapCache implements ShardCache<Object, Object> ,SyncBeanCreater{
	private int size = 0;
	private Map<String, Map<Object, Object>> cachesMap;

	public LRUMapShardCache(String defaultShardName) {
		cachesMap = new HashMap<>();
		cachesMap.put(defaultShardName, super.cacheMap);
	}

	public LRUMapShardCache(String defaultShardName, Integer size) {
		super(size);
		this.size = size;
		cachesMap = new HashMap<>();
		cachesMap.put(defaultShardName, super.cacheMap);
	}
	
	@Override
	public void put(String shardName, Object key, Object value) throws LzRuntimeException {
		putCount++;
		takeCache(shardName).put(key, value);
	}

	@Override
	public void put(String shardName, Object key, Object value, long expiring, TimeUnit timeUnit)
			throws LzRuntimeException {
		putCount++;
		takeCache(shardName).put(key, value);
	}

	@Override
	public void remove(String shardName, Object key) throws LzRuntimeException {
		removeCount++;
		takeCache(shardName).remove(key);
	}

	@Override
	public Object get(String shardName, Object key) throws LzRuntimeException {
		getCount++;
		Object value = takeCache(shardName).get(key);
		if (value != null) {
			hitCount++;
		}
		return value;
	}

	@Override
	public boolean exists(String shardName, Object key) {
		return takeCache(shardName).containsKey(key);
	}

	@Override
	public JSONObject size(String shardName) {
		JSONObject size = new JSONObject();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, takeCache(shardName).size());
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	public void clear(String shardName) throws LzRuntimeException {
		takeCache(shardName).clear();
	}

	private Map<Object, Object> takeCache(String shardName) {
		return syncCreate(() -> cachesMap.get(shardName), () -> cachesMap.put(shardName,generateLRUMap(size)));
	}
}
