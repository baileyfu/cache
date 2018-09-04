package xcache.local.map;

import java.util.Map;

import xcache.em.TimeUnit;
import xcache.local.MapCache;
import xcache.local.SyncLRUMapGenerateAble;

/**
 * 基于LRUMap实现；不支持expiring
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-26 13:47
 * @param <K>
 * @param <V>
 */
public class LRUMapCache<K, V> implements MapCache<K, V>, SyncLRUMapGenerateAble {
	private Map<K, V> cacheMap;

	@SuppressWarnings("unchecked")
	public LRUMapCache() {
		cacheMap = generateLRUMap();
	}

	@SuppressWarnings("unchecked")
	public LRUMapCache(int size) {
		cacheMap = generateLRUMap(size);
	}

	@SuppressWarnings("unchecked")
	public LRUMapCache(Map<K, V> cacheMap) {
		this.cacheMap = generateLRUMap(cacheMap);
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
