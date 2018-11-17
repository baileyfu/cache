package com.lz.components.cache;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.core.CacheManagerFactory;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.exception.LzRuntimeException;
import com.lz.components.common.util.NumberFormat;

/**
 * 缓存抽象类
 * 
 * @author fuli
 * @date 2018年9月18日
 * @version 1.0.0
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {
	protected String name;
	protected boolean useable;
	protected volatile long getCount;
	protected volatile long hitCount;
	protected volatile long putCount;
	protected volatile long removeCount;

	public AbstractCache() {
		useable = true;
		name = CacheManagerFactory.DEFAULT_CACHE_MANAGER_NAME;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean useable() {
		return useable;
	}

	public void enable() {
		this.useable = true;
	}

	public void disable() {
		this.useable = false;
	}
	@Override
	public String cacheName() {
		return name;
	}
	@Override
	public V get(K key) throws LzRuntimeException {
		getCount++;
		V v = doGet(key);
		if (v != null) {
			hitCount++;
		}
		return v;
	}
	@Override
	public void put(K key, V value) throws LzRuntimeException {
		put(key, value, 0l, null);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		putCount++;
		doPut(key, value, expiring, timeUnit);
	}
	@Override
	public void remove(K key) throws LzRuntimeException {
		removeCount++;
		doRemove(key);
	}
	@Override
	public JSONObject size() {
		JSONObject size = new JSONObject();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, 0);
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	};
	@Override
	public JSONObject report() {
		JSONObject report = new JSONObject();
		report.put(CacheParams.CACHE_NAME.NAME, cacheName());
		JSONObject size=size();
		long memorySize=size.getLongValue(CacheParams.SIZE_MEMORY.NAME);
		size.put(CacheParams.SIZE_MEMORY.NAME, NumberFormat.format(String.valueOf(memorySize / 1024),"0.## Kb"));
		report.put(CacheParams.SIZE.NAME, size);
		report.put(CacheParams.TOTAL_GET.NAME, getCount);
		report.put(CacheParams.TOTAL_PUT.NAME, putCount);
		report.put(CacheParams.TOTAL_REMOVE.NAME, removeCount);
		report.put(CacheParams.HIT_RATIO.NAME,NumberFormat.format(String.valueOf(getCount == 0 ? 1d : (hitCount / getCount)),"##.##%"));
		return report;
	}
	
	protected abstract V doGet(K key) throws LzRuntimeException;
	protected abstract void doPut(K key, V value, long expiring, TimeUnit timeUnit) throws LzRuntimeException;
	protected abstract void doRemove(K key) throws LzRuntimeException;
}
