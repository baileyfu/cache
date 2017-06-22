package xcache.bean;

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
	 * @throws Exception
	 */
	public EHCache(String ehcacheName) throws Exception {
		init(null, ehcacheName);
	}

	/**
	 * 
	 * @param configurationFileName
	 *            配置文件绝对路径
	 * @param ehcacheName
	 * @throws Exception
	 */
	public EHCache(String configurationFileName, String ehcacheName) throws Exception {
		CacheManager cacheManager = CacheManager.create(configurationFileName);
		init(cacheManager, ehcacheName);
	}

	/**
	 * 
	 * @param config
	 * @param ehcacheName
	 * @throws Exception
	 */
	public EHCache(Configuration config, String ehcacheName) throws Exception {
		CacheManager cacheManager = CacheManager.create(config);
		init(cacheManager, ehcacheName);
	}

	public EHCache(InputStream inputStream, String ehcacheName) throws Exception {
		CacheManager cacheManager = CacheManager.create(inputStream);
		init(cacheManager, ehcacheName);
	}

	private void init(CacheManager cacheManager, String ehcacheName) throws Exception {
		cache = (cacheManager == null ? CacheManager.create() : cacheManager).getCache(ehcacheName);
		if (cache == null) {
			throw new Exception("ehcache '" + ehcacheName + "' is not exist");
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
	public V get(K key) throws Exception {
		Element element = cache.get(key);
		return element == null ? null : (V) element.getObjectValue();
	}

	public void put(K key, V value) throws Exception {
		Element element = new Element(key, value);
		cache.remove(key);
		cache.put(element);
	}

	@Override
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws Exception {
		int expiringSeconds = timeUnit.toSeconds(expiring);
		Element element = new Element(key, value, expiringSeconds, expiringSeconds);
		cache.remove(key);
		cache.put(element);
	}

	@Override
	public boolean exists(K key) {
		return cache.isKeyInCache(key);
	}

	public void remove(K key) throws Exception {
		cache.remove(key);
	}

	public void clear() throws Exception {
		cache.removeAll();
	}

	public int size() {
		return cache.getSize();
	}
}
