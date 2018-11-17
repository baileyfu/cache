package com.lz.components.cache.local.map.shard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.ShardCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.local.map.MultiMapAutoCleanCache;
import com.lz.components.common.beanutil.SyncBeanCreater;
import com.lz.components.common.exception.LzRuntimeException;

public class MultiMapAutoCleanShardCache extends MultiMapAutoCleanCache implements ShardCache<Object, Object> ,SyncBeanCreater{
	private Map<String,Map<Integer, Map<Object, Entity>>> cachesMap;
	public MultiMapAutoCleanShardCache(String defaultShardName){
		cachesMap=new HashMap<>();
		cachesMap.put(defaultShardName, super.cacheMap);
	}
	public MultiMapAutoCleanShardCache(String defaultShardName,Integer clearInterval) {
		super(clearInterval);
		cachesMap=new HashMap<>();
		cachesMap.put(defaultShardName, super.cacheMap);
	}
	
	@Override
	public void put(String shardName, Object key, Object value) throws LzRuntimeException {
		putCount++;
		takeCache(shardName,key).put(key, new Entity(value));
	}

	@Override
	public void put(String shardName, Object key, Object value, long expiring, TimeUnit timeUnit)throws LzRuntimeException {
		putCount++;
		takeCache(shardName,key).put(key, new Entity(value,timeUnit.toMilliseconds(expiring)));
	}

	@Override
	public void remove(String shardName, Object key) throws LzRuntimeException {
		removeCount++;
		takeCache(shardName,key).remove(key);
	}

	@Override
	public Object get(String shardName, Object key) throws LzRuntimeException {
		getCount++;
		Object value = takeCache(shardName, key).get(key).getElement();
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
	@Override
	protected void clearExpiring() {
		cachesMap.values().parallelStream().forEach((cache) -> {
			cache.keySet().parallelStream().forEach((subKey) -> {
				Map<Object, Entity> sub = cache.get(subKey);
				sub.keySet().parallelStream().filter((k) -> {
					Entity value = sub.get(k);
					return value == null || value.unAble();
				}).collect(Collectors.toList()).forEach(sub::remove);
			});
		});
	}
	private Map<Integer, Map<Object, Entity>> takeCaches(String shardName) {
		return syncCreate(() -> cachesMap.get(shardName),
				() -> cachesMap.put(shardName, new HashMap<Integer, Map<Object, Entity>>()));
	}

	private Map<Object, Entity> takeCache(String shardName, Object key) {
		Map<Integer, Map<Object, Entity>> cache = syncCreate(() -> cachesMap.get(shardName),
				() -> cachesMap.put(shardName, new HashMap<Integer, Map<Object, Entity>>()));
		Integer subKey = kenGenerator.make(key);
		Map<Object, Entity> subMap = cache.get(subKey);
		if (subMap == null) {
			subMap = new ConcurrentHashMap<>();
			cache.put(subKey, subMap);
		}
		return subMap;
	}
}
