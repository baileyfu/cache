package com.lz.components.cache.local.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lz.components.cache.local.MapCache;

/**
 * 基于Map的Cache;需手动清理
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 17:18
 */
public class SingleMapCache extends MapCache {
	protected Map<Object, Object> cacheMap;

	public SingleMapCache() {
		cacheMap = new ConcurrentHashMap<>();
	}
	public SingleMapCache(Map<Object, Object> cacheMap) {
		this.cacheMap = new ConcurrentHashMap<>(cacheMap);
	}
	@Override
	public Map<Object, Object> value() {
		return cacheMap;
	}
}
