package xcache.local;

import java.io.InputStream;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import xcache.LocalCache;
import xcache.em.TimeUnit;

/**
 * 基于ehcache实现的cache
 * 
 * @author bailey.fu
 * @date Dec 16, 2010
 * @version 1.0
 */
public class EHCache<K, V> implements LocalCache<K,V> {
	private net.sf.ehcache.Cache cache;

	/**
	 * 
	 * @param ehcacheName
	 * @throws RuntimeException
	 */
	public EHCache(String ehcacheName) throws RuntimeException {
		init(null, ehcacheName);
	}

	/**
	 * 
	 * @param configurationFileName
	 *            配置文件绝对路径
	 * @param ehcacheName
	 * @throws RuntimeException
	 */
	public EHCache(String configurationFileName, String ehcacheName) throws RuntimeException {
		CacheManager cacheManager = CacheManager.create(configurationFileName);
		init(cacheManager, ehcacheName);
	}

	/**
	 * 
	 * @param config
	 * @param ehcacheName
	 * @throws RuntimeException
	 */
	public EHCache(Configuration config, String ehcacheName) throws RuntimeException {
		CacheManager cacheManager = CacheManager.create(config);
		init(cacheManager, ehcacheName);
	}

	public EHCache(InputStream inputStream, String ehcacheName) throws RuntimeException {
		CacheManager cacheManager = CacheManager.create(inputStream);
		init(cacheManager, ehcacheName);
	}

	private void init(CacheManager cacheManager, String ehcacheName) throws RuntimeException {
		cache = (cacheManager == null ? CacheManager.create() : cacheManager).getCache(ehcacheName);
		if (cache == null) {
			throw new RuntimeException("ehcache '" + ehcacheName + "' is not exist");
		}
	}

	/** 直接操作net.sf.ehcache.Cache */
	public net.sf.ehcache.Cache getEhcache() {
		return cache;
	}

	public String getEhcacheName() {
		return cache == null ? null : cache.getName();
	}

	@SuppressWarnings("unchecked")
	public V get(K key) throws RuntimeException {
		Element element = cache.get(key);
		return element == null ? null : (V) element.getObjectValue();
	}

	public void put(K key, V value) throws RuntimeException {
		Element element = new Element(key, value);
		cache.remove(key);
		cache.put(element);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws RuntimeException {
		int expiringSeconds = timeUnit.toSeconds(expiring);
		Element element = new Element(key, value, expiringSeconds, expiringSeconds);
		cache.remove(key);
		cache.put(element);
	}

	@Override
	public boolean exists(K key) {
		return cache.isKeyInCache(key);
	}

	public void remove(K key) throws RuntimeException {
		cache.remove(key);
	}

	public void clear() throws RuntimeException {
		cache.removeAll();
	}

	public int size() {
		return cache.getSize();
	}
}
