package xcache.local.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xcache.LocalCache;
import xcache.em.TimeUnit;
import xcache.local.MapCache;

/**
 * 基于Map的Cache;需手动清理
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 17:18
 */
public class SingleMapCache<K, V> implements LocalCache<K, V>, MapCache<K, V> {
	private Map<K, V> cacheMap;

	public SingleMapCache() {
		cacheMap = new ConcurrentHashMap<>();
	}

	public SingleMapCache(Map<K, V> cacheMap) {
		this.cacheMap = new ConcurrentHashMap<>(cacheMap);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws RuntimeException {
		put(key, value);
	}

	@Override
	public Map<K, V> value() {
		return cacheMap;
	}

}
