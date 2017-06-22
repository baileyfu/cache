package xcache.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xcache.LocalCache;
import xcache.em.TimeUnit;

/**
 * 基于Map的Cache;需手动清理
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 17:18
 */
public class SingleMapCache<K, V> implements LocalCache<K, V> {
	private Map<K, V> cacheMap;

	public SingleMapCache() {
		cacheMap = new ConcurrentHashMap<>();
	}

	public SingleMapCache(Map<K, V> cacheMap) {
		this.cacheMap = new ConcurrentHashMap<>(cacheMap);
	}

	@Override
	public void put(K key, V value) throws Exception {
		cacheMap.put(key, value);
		System.out.println("A-------------->"+cacheMap);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws Exception {
		cacheMap.put(key, value);
		System.out.println("B-------------->"+cacheMap);
	}

	@Override
	public void remove(K key) throws Exception {
		cacheMap.remove(key);
		System.out.println("C-------------->"+key+"==="+cacheMap);
	}

	@Override
	public V get(K key) throws Exception {
		return cacheMap.get(key);
	}

	@Override
	public int size() {
		return cacheMap.size();
	}

	@Override
	public void clear() throws Exception {
		cacheMap.clear();
	}

	@Override
	public boolean exists(K key) {
		return cacheMap.containsKey(key);
	}
}
