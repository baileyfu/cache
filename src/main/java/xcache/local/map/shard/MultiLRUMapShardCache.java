package com.lz.components.cache.local.map.shard;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.ShardCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.local.map.MultiLRUMapCache;
import com.lz.components.common.beanutil.SyncBeanCreater;
import com.lz.components.common.exception.LzRuntimeException;

public class MultiLRUMapShardCache extends MultiLRUMapCache implements ShardCache<Object, Object> ,SyncBeanCreater{
	private Map<String,Map<Integer, Map<Object, Object>>> cachesMap;

	public MultiLRUMapShardCache(String defaultShardName) {
		cachesMap = new HashMap<>();
		cachesMap.put(defaultShardName, super.cacheMap);
	}
	public MultiLRUMapShardCache(String defaultShardName,Integer subSize){
		super(subSize);
		cachesMap = new HashMap<>();
		cachesMap.put(defaultShardName, super.cacheMap);
	}
	
	@Override
	public void put(String shardName, Object key, Object value) throws LzRuntimeException {
		putCount++;
		takeCache(shardName,key).put(key, value);
	}

	@Override
	public void put(String shardName, Object key, Object value, long expiring, TimeUnit timeUnit)
			throws LzRuntimeException {
		putCount++;
		takeCache(shardName,key).put(key, value);
	}

	@Override
	public void remove(String shardName, Object key) throws LzRuntimeException {
		removeCount++;
		takeCache(shardName, key).remove(key);
	}

	@Override
	public Object get(String shardName, Object key) throws LzRuntimeException {
		getCount++;
		Object value = takeCache(shardName, key).get(key);
		if (value != null) {
			hitCount++;
		}
		return value;
	}

	@Override
	public boolean exists(String shardName, Object key) {
		return takeCache(shardName,key).containsKey(key);
	}

	@Override
	public JSONObject size(String shardName) {
		JSONObject size = new JSONObject();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, takeCaches(shardName).values().stream().reduce(0, (i, c) -> i + c.size(), (u, v) -> u));
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	public void clear(String shardName) throws LzRuntimeException {
		takeCaches(shardName).clear();
	}

	private Map<Integer, Map<Object, Object>> takeCaches(String shardName) {
		return syncCreate(() -> cachesMap.get(shardName),
				() -> cachesMap.put(shardName, new HashMap<Integer, Map<Object, Object>>()));
	}
	private Map<Object, Object> takeCache(String shardName, Object key) {
		Map<Integer, Map<Object, Object>> cache = syncCreate(() -> cachesMap.get(shardName),
				() -> cachesMap.put(shardName, new HashMap<Integer, Map<Object, Object>>()));
		return cache.get(kenGenerator.make(key));
	}
}
