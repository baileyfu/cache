package com.lz.components.cache.local.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.local.AutoCleanAbleMapCache;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 基于Map的Cache;key不做hash直接存储
 * 
 * @author bailey.fu
 * @date Dec 17, 2010
 * @update 2017-01-03 17:42
 * @version 1.8
 * @description 自定义缓存
 */
public class SingleMapAutoCleanCache extends AutoCleanAbleMapCache{
	protected Map<Object, Entity> cacheMap;
	public SingleMapAutoCleanCache(){
		super();
		init();
	}
	
	/**
	 * 清理间隔(单位：分钟)
	 * 
	 * @param clearInterval
	 */
	public SingleMapAutoCleanCache(Integer clearInterval) {
		super(clearInterval);
		init();
	}
	private void init(){
		cacheMap = new ConcurrentHashMap<>();
	}
	protected Object doGet(Object key) throws LzRuntimeException {
		Entity entity = cacheMap.get(key);
		return entity == null ? null : entity.getElement();
	}

	protected void doPut(Object key, Object value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		if (key != null && value != null) {
			if (expiring > 0) {
				cacheMap.put(key, new Entity(value, timeUnit.toMilliseconds(expiring)));
			}else{
				cacheMap.put(key, new Entity(value));
			}
		}
	}

	@Override
	public boolean exists(Object key) {
		return cacheMap.containsKey(key);
	}

	protected void doRemove(Object key) throws LzRuntimeException {
		cacheMap.remove(key);
	}

	public void clear() throws LzRuntimeException {
		cacheMap.clear();
	}

	@Override
	public JSONObject size() {
		JSONObject size = super.size();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, this.size());
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	protected void clearExpiring() {
		cacheMap.keySet().parallelStream().filter((key) -> {
			Entity value = cacheMap.get(key);
			return value == null || value.unAble();
		}).collect(Collectors.toList()).forEach(cacheMap::remove);
	}

	@Override
	public Map<Object, Object> value() {
		return null;
	}
}
