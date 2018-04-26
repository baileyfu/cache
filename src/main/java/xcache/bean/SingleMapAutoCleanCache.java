package xcache.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import xcache.em.TimeUnit;

/**
 * 基于Map的Cache;key不做hash直接存储
 * 
 * @author bailey.fu
 * @date Dec 17, 2010
 * @update 2017-01-03 17:42
 * @version 1.8
 * @description 自定义缓存
 */
public class SingleMapAutoCleanCache<K, V> extends AutoCleanAbleCache<K, V> {
	private Map<K, Entity> cacheMap;

	public SingleMapAutoCleanCache(){
		super();
		init();
	}
	
	/**
	 * 清理间隔(单位：分钟)
	 * 
	 * @param clearInterval
	 */
	public SingleMapAutoCleanCache(int clearInterval) {
		super(clearInterval);
		init();
	}
	private void init(){
		cacheMap = new ConcurrentHashMap<>();
	}
	public V get(K key) throws Exception {
		Entity entity = cacheMap.get(key);
		return entity == null ? null : entity.getElement();
	}

	public void put(K key, V value) throws Exception {
		if (key != null && value != null) {
			cacheMap.put(key, new Entity(value));
		}
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws Exception {
		if (key != null && value != null) {
			cacheMap.put(key, new Entity(value, timeUnit.toMilliseconds(expiring)));
		}
	}

	@Override
	public boolean exists(K key) {
		return cacheMap.containsKey(key);
	}

	public void remove(K key) throws Exception {
		cacheMap.remove(key);
	}

	public void clear() throws Exception {
		cacheMap.clear();
	}

	public int size() {
		return cacheMap.size();
	}

	@Override
	protected void clearExpiring() {
		cacheMap.keySet().parallelStream().filter((key) -> {
			Entity value = cacheMap.get(key);
			return value == null || value.unAble();
		}).collect(Collectors.toList()).forEach(cacheMap::remove);
	}
}
