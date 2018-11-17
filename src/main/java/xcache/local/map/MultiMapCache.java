package com.lz.components.cache.local.map;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.key.CacheKeyTransformer;
import com.lz.components.cache.key.LimitedHashCodeTransformer;
import com.lz.components.cache.local.MapCache;
import com.lz.components.common.exception.LzRuntimeException;

public class MultiMapCache extends MapCache {
	protected CacheKeyTransformer kenGenerator;
	protected Map<Integer, Map<Object, Object>> cacheMap;
	private volatile int size;

	public MultiMapCache() {
		kenGenerator = new LimitedHashCodeTransformer();
		cacheMap = new HashMap<>();
	}

	@Override
	protected void doPut(Object key, Object value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		Map<Object, Object> subMap = takeSub(kenGenerator.make(key));
		if (!subMap.containsKey(key)) {
			size++;
		}
		subMap.put(key, value);
	}

	@Override
	public boolean exists(Object key) {
		return takeSub(kenGenerator.make(key)).containsKey(key);
	}

	@Override
	protected void doRemove(Object key) throws LzRuntimeException {
		if (takeSub(kenGenerator.make(key)).remove(key) != null)
			size = size > 1 ? size - 1 : size;
	}

	@Override
	protected Object doGet(Object key) throws LzRuntimeException {
		return takeSub(kenGenerator.make(key)).get(key);
	}

	private Map<Object, Object> takeSub(Integer key) {
		Map<Object, Object> subMap = cacheMap.get(key);
		if (subMap == null) {
			subMap = new ConcurrentHashMap<>();
			cacheMap.put(key, subMap);
		}
		return subMap;
	}

	@Override
	public JSONObject size() {
		JSONObject size = super.size();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, this.size);
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	public void clear() throws LzRuntimeException {
		cacheMap.clear();
	}

	public void setKenGenerator(CacheKeyTransformer kenGenerator) {
		this.kenGenerator = kenGenerator;
	}
	public Map<Integer, Map<Object, Object>> values() {
		return cacheMap;
	}
	@Override
	public Map<Object, Object> value() {
		return null;
	}
}
