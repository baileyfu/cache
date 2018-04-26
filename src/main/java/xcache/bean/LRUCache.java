package xcache.bean;

import java.util.Map;

import xcache.em.TimeUnit;

/**
 * 基于LRUMap实现；不支持expiring
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-26 13:47
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> implements MapCache<K, V>, SyncLRUMapGenerateAble {
	private Map<K, V> cacheMap;

	@SuppressWarnings("unchecked")
	public LRUCache() {
		cacheMap = generateLRUMap();
	}

	@SuppressWarnings("unchecked")
	public LRUCache(int size) {
		cacheMap = generateLRUMap(size);
	}

	@SuppressWarnings("unchecked")
	public LRUCache(Map<K, V> cacheMap) {
		this.cacheMap = generateLRUMap(cacheMap);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws Exception {
		put(key, value);
	}

	@Override
	public Map<K, V> value() {
		return cacheMap;
	}

}
