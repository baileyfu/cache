package xcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commons.fun.Supplier;
import xcache.em.TimeUnit;

/**
 * 缓存管理器
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 16:00
 */
public final class CacheManager {
	private static Logger logger = LoggerFactory.getLogger(CacheManager.class);
	private static CacheManager cacheManager = null;

	private final LocalCache<Object, Object> localCache;
	private final RemoteCache remoteCache;

	private CacheManager(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		this.localCache = localCache;
		this.remoteCache = remoteCache;
	}

	private Object $dol(Cache<Object, Object> cache, Supplier<Object> spl) throws Exception {
		if (cache != null) {
			return spl.get();
		}
		logger.warn("No LocalCache be found !");
		return null;
	}

	private Object $dor(Cache<Object, Object> cache, Supplier<Object> spl) throws Exception {
		if (cache != null) {
			return spl.get();
		}
		logger.warn("No RemoteCache be found !");
		return null;
	}

	public Object getLocal(Object key) throws Exception {
		return $dol(localCache, () -> localCache.get(key));
	}

	public Object getRemote(Object key) throws Exception {
		return $dor(remoteCache, () -> remoteCache.get(key));
	}

	public void putToLocal(Object key, Object value) throws Exception {
		// TODO 以异步形式放入
		$dol(localCache, () -> {
			localCache.put(key, value);
			return null;
		});
	}

	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		// TODO 以异步形式放入
		$dol(localCache, () -> {
			localCache.put(key, value, expiring, timeUnit);
			return null;
		});
	}

	public void putToRemote(Object key, Object value) throws Exception {
		// TODO 以异步形式放入
		$dor(remoteCache, () -> {
			remoteCache.put(key, value);
			return null;
		});
	}

	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		// TODO 以异步形式放入
		$dor(remoteCache, () -> {
			remoteCache.put(key, value, expiring, timeUnit);
			return null;
		});
	}

	public void removeLocal(Object key) throws Exception {
		// TODO 以异步形式删除
		$dol(localCache, () -> {
			localCache.remove(key);
			return null;
		});
	}

	public void removeRemote(Object key) throws Exception {
		// TODO 以异步形式删除
		$dol(remoteCache, () -> {
			remoteCache.remove(key);
			return null;
		});
	}

	private static synchronized void syncInit(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		if (cacheManager == null) {
			cacheManager = new CacheManager(localCache, remoteCache);
		}
	}

	/**
	 * 调用此方法的前提是必须事先调用了create()方法
	 * 
	 * @return
	 */
	public static CacheManager getInstance() {
		return cacheManager;
	}

	public static CacheManager create(LocalCache<Object, Object> localCache) {
		if (cacheManager == null) {
			syncInit(localCache, null);
		}
		return cacheManager;
	}

	public static CacheManager create(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		if (cacheManager == null) {
			syncInit(localCache, remoteCache);
		}
		return cacheManager;
	}
}
