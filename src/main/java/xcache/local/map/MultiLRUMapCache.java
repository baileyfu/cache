package xcache.local.map;

import java.util.HashMap;
import java.util.Map;

import xcache.em.TimeUnit;
import xcache.key.CacheKeyTransformer;
import xcache.key.LimitedDumpFieldTransformer;
import xcache.local.MapCache;
import xcache.local.SyncLRUMapGenerateAble;

public class MultiLRUMapCache<K, V> implements MapCache<K, V>, SyncLRUMapGenerateAble {
	private CacheKeyTransformer kenGenerator;
	private Map<Integer, Map<K, V>> cacheMap;
	private volatile int size;
	private int subSize;

	public MultiLRUMapCache() {
		init(0, new LimitedDumpFieldTransformer());
	}

	public MultiLRUMapCache(int subSize) {
		init(subSize, new LimitedDumpFieldTransformer());
	}

	private void init(int subSize, CacheKeyTransformer kenGenerator) {
		this.subSize = subSize;
		this.kenGenerator = kenGenerator;
		cacheMap = new HashMap<>();
	}

	@Override
	public void put(K key, V value) throws RuntimeException {
		Map<K, V> subMap = takeSub(kenGenerator.make(key));
		if (!subMap.containsKey(key)) {
			size++;
		}
		subMap.put(key, value);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws RuntimeException {
		put(key, value);
	}

	@Override
	public boolean exists(K key) {
		return takeSub(kenGenerator.make(key)).containsKey(key);
	}

	@Override
	public void remove(K key) throws RuntimeException {
		if (takeSub(kenGenerator.make(key)).remove(key) != null)
			size = size > 1 ? size - 1 : size;
	}

	@Override
	public V get(K key) throws RuntimeException {
		return takeSub(kenGenerator.make(key)).get(key);
	}

	@SuppressWarnings("unchecked")
	private Map<K, V> takeSub(Integer key) {
		Map<K, V> subMap = cacheMap.get(key);
		if (subMap == null) {
			subMap = generateLRUMap(subSize);
			cacheMap.put(key, subMap);
		}
		return subMap;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() throws RuntimeException {
		cacheMap.clear();
	}

	public void setKenGenerator(CacheKeyTransformer kenGenerator) {
		this.kenGenerator = kenGenerator;
	}
	public Map<Integer, Map<K, V>> values() {
		return cacheMap;
	}
	@Override
	public Map<K, V> value() {
		return null;
	}
}
