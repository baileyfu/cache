package com.lz.components.cache.local.map;

import java.util.Map;

import com.lz.components.cache.local.MapCache;
import com.lz.components.cache.local.SyncLRUMapGenerateAble;

/**
 * 基于LRUMap实现；不支持expiring
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-26 13:47
 * @param <K>
 * @param <V>
 */
public class LRUMapCache extends MapCache implements SyncLRUMapGenerateAble {
	protected Map<Object, Object> cacheMap;

	public LRUMapCache() {
		cacheMap = generateLRUMap();
	}

	public LRUMapCache(Integer size) {
		cacheMap = generateLRUMap(size);
	}

	public LRUMapCache(Map<Object, Object> cacheMap) {
		this.cacheMap = generateLRUMap(cacheMap);
	}
	@Override
	protected Map<Object, Object> value() {
		return cacheMap;
	}
}
