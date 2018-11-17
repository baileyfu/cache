package com.lz.components.cache.local;

import java.io.InputStream;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.LocalCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.code.ExceptionCode;
import com.lz.components.common.exception.LzRuntimeException;
import com.lz.components.common.util.Asserts;
import com.lz.components.common.util.NumberFormat;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.statistics.StatisticsGateway;

/**
 * 基于ehcache实现的cache
 * 
 * @author bailey.fu
 * @date Dec 16, 2010
 * @version 1.0
 */
public class EHCache extends LocalCache {
	protected CacheManager cacheManager;
	protected net.sf.ehcache.Cache cache;

	/**
	 * 
	 * @param ehcacheName
	 * @throws LzRuntimeException
	 */
	public EHCache(String ehcacheName) throws LzRuntimeException {
		cacheManager = CacheManager.create();
		init(ehcacheName);
	}

	/**
	 * 
	 * @param configurationFileName
	 *            配置文件绝对路径
	 * @param ehcacheName
	 * @throws LzRuntimeException
	 */
	public EHCache(String configurationFileName, String ehcacheName) throws LzRuntimeException {
		cacheManager = CacheManager.create(configurationFileName);
		init(ehcacheName);
	}

	/**
	 * 
	 * @param config
	 * @param ehcacheName
	 * @throws LzRuntimeException
	 */
	public EHCache(Configuration config, String ehcacheName) throws LzRuntimeException {
		cacheManager = CacheManager.create(config);
		init(ehcacheName);
	}

	public EHCache(InputStream inputStream, String ehcacheName) throws LzRuntimeException {
		cacheManager = CacheManager.create(inputStream);
		init(ehcacheName);
	}

	private void init(String ehcacheName) throws LzRuntimeException {
		try {
			cache = cacheManager.getCache(ehcacheName);
		} catch (Exception e) {
			throw new LzRuntimeException(e);
		}
		Asserts.check(cache != null, ExceptionCode.FAILED);
	}

	/** 直接操作net.sf.ehcache.Cache */
	public net.sf.ehcache.Cache getEhcache() {
		return cache;
	}

	public String getEhcacheName() {
		return cache == null ? null : cache.getName();
	}

	@Override
	public boolean exists(Object key) {
		return cache.isKeyInCache(key);
	}

	public void clear() throws LzRuntimeException {
		cache.removeAll();
	}

	public JSONObject size() {
		JSONObject size = super.size();
		size.put(CacheParams.SIZE_CAPACITY.NAME, cache.getCacheConfiguration().getMaxEntriesInCache());
		size.put(CacheParams.SIZE_QUANTITY.NAME, cache.getSize());
		size.put(CacheParams.SIZE_MEMORY.NAME, cache.getStatistics().getLocalHeapSizeInBytes());
		return size;
	}

	@Override
	public JSONObject report() {
		JSONObject report = super.report();
		StatisticsGateway statistics = cache.getStatistics();
		report.put(CacheParams.TOTAL_GET.NAME, statistics.cacheGetOperation().count());
		report.put(CacheParams.TOTAL_PUT.NAME, statistics.cachePutAddedCount());
		report.put(CacheParams.TOTAL_REMOVE.NAME, removeCount);
		report.put(CacheParams.HIT_RATIO.NAME, NumberFormat.format(String.valueOf(statistics.cacheHitRatio()),"##.##%"));
		return report;
	}
	@Override
	protected Object doGet(Object key) throws LzRuntimeException {
		Element element = cache.get(key);
		return element == null ? null : element.getObjectValue();
	}
	@Override
	protected void doPut(Object key, Object value, long expiring, TimeUnit timeUnit) {
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
	protected void doRemove(Object key) throws LzRuntimeException {
		cache.remove(key);
	}
}
