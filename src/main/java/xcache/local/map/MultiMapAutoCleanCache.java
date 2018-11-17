package com.lz.components.cache.local.map;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.key.CacheKeyTransformer;
import com.lz.components.cache.key.LimitedHashCodeTransformer;
import com.lz.components.cache.local.AutoCleanAbleMapCache;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 对key做hash存于多个Map
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 16:22
 */
public class MultiMapAutoCleanCache extends AutoCleanAbleMapCache {
	protected CacheKeyTransformer kenGenerator;
	protected Map<Integer, Map<Object, Entity>> cacheMap;
	private volatile int size;

	public MultiMapAutoCleanCache() {
		super();
		init();
	}

	/**
	 * @param clearInterval
	 *            清理间隔(单位：分钟)
	 */
	public MultiMapAutoCleanCache(Integer clearInterval) {
		super(clearInterval);
		init();
	}

	private void init() {
		cacheMap = new HashMap<>();
		size = 0;
		kenGenerator = new LimitedHashCodeTransformer();
	}

	@Override
	protected void doPut(Object key, Object value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		Map<Object, Entity> subMap = takeSub(kenGenerator.make(key));
		if (!subMap.containsKey(key)) {
			size++;
		}
		if (expiring > 0) {
			subMap.put(key, new Entity(value, timeUnit.toMilliseconds(expiring)));
		} else {
			subMap.put(key, new Entity(value));
		}
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
		return takeSub(kenGenerator.make(key)).get(key).getElement();
	}

	private Map<Object, Entity> takeSub(Integer key) {
		Map<Object, Entity> subMap = cacheMap.get(key);
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

	@Override
	protected void clearExpiring() {
		cacheMap.keySet().parallelStream().forEach((key) -> {
			Map<Object, Entity> sub = takeSub(key);
			sub.keySet().parallelStream().filter((k) -> {
				Entity value = sub.get(k);
				return value == null || value.unAble();
			}).collect(Collectors.toList()).forEach(sub::remove);
		});
	}

	public void setKenGenerator(CacheKeyTransformer kenGenerator) {
		this.kenGenerator = kenGenerator;
	}

	@Override
	public Map<Object, Object> value() {
		return null;
	}
}
