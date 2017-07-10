package xcache.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import xcache.em.TimeUnit;
import xcache.key.CacheKeyTransformer;
import xcache.key.LimitedDumpFieldTransformer;

/**
 * 对key做hash存于多个Map
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 16:22
 */
public class MultiMapAutoCleanCache<K, V> extends AutoCleanAbleCache<K, V> {
	private CacheKeyTransformer kenGenerator;
	private Map<Integer, Map<K, Entity>> cacheMap;
	private volatile int size;

	public MultiMapAutoCleanCache() {
		super();
		init();
	}

	/**
	 * @param clearInterval
	 *            清理间隔(单位：分钟)
	 */
	public MultiMapAutoCleanCache(int clearInterval) {
		super(clearInterval);
		init();
	}

	private void init() {
		cacheMap = new HashMap<>();
		size = 0;
		kenGenerator = new LimitedDumpFieldTransformer();
	}

	@Override
	public void put(K key, V value) throws Exception {
		Map<K, Entity> subMap = takeSub(kenGenerator.make(key));
		if (!subMap.containsKey(key)) {
			size++;
		}
		subMap.put(key, new Entity(value));
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws Exception {
		Map<K, Entity> subMap = takeSub(kenGenerator.make(key));
		if (!subMap.containsKey(key)) {
			size++;
		}
		subMap.put(key, new Entity(value, timeUnit.toMilliseconds(expiring)));
	}

	@Override
	public boolean exists(K key) {
		return takeSub(kenGenerator.make(key)).containsKey(key);
	}

	@Override
	public void remove(K key) throws Exception {
		if (takeSub(kenGenerator.make(key)).remove(key) != null)
			size = size > 1 ? size - 1 : size;
	}

	@Override
	public V get(K key) throws Exception {
		return takeSub(kenGenerator.make(key)).get(key).getElement();
	}

	private Map<K, Entity> takeSub(Integer key) {
		Map<K, Entity> subMap = cacheMap.get(key);
		if (subMap == null) {
			subMap = new ConcurrentHashMap<>();
			cacheMap.put(key, subMap);
		}
		return subMap;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() throws Exception {
		cacheMap.clear();
	}

	@Override
	protected void clearExpiring() {
		cacheMap.keySet().parallelStream().forEach((key) -> {
			Map<K, Entity> sub = takeSub(key);
			sub.keySet().parallelStream().filter((k) -> {
				Entity value = sub.get(key);
				return value == null || value.unAble();
			}).collect(Collectors.toList()).forEach(sub::remove);
		});
	}

	public void setKenGenerator(CacheKeyTransformer kenGenerator) {
		this.kenGenerator = kenGenerator;
	}
}
