package xcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commons.fun.NAFunction;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
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

	private final SingleObserver<NAFunction> cacheObserver;

	private CacheManager(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		this.localCache = localCache;
		this.remoteCache = remoteCache;
		if (this.localCache == null) {
			logger.warn("No LocalCache be found !");
		}
		if (this.remoteCache == null) {
			logger.warn("No RemoteCache be found !");
		}
		cacheObserver = new SingleObserver<NAFunction>() {
			@Override
			public void onSubscribe(Disposable d) {
			}
			@Override
			public void onSuccess(NAFunction naf) {
				try {
					naf.apply();
				} catch (Exception e) {
					logger.error("XCache putting error !", e);
				}
			}
			@Override
			public void onError(Throwable e) {
				logger.error("XCache subscribe error !", e);
			}
		};
	}

	public Object getLocal(Object key) throws Exception {
		return localCache == null ? null : localCache.get(key);
	}

	public Object getRemote(Object key) throws Exception {
		return remoteCache == null ? null : remoteCache.get(key);
	}

	public void putToLocal(Object key, Object value) throws Exception {
		if (localCache != null)
			$async(() -> localCache.put(key, value));
	}

	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		if (localCache != null)
			$async(() -> localCache.put(key, value, expiring, timeUnit));
	}

	public void putToRemote(Object key, Object value) throws Exception {
		if (remoteCache != null)
			$async(() -> remoteCache.put(key, value));
	}

	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		if (remoteCache != null)
			$async(() -> remoteCache.put(key, value, expiring, timeUnit));
	}

	public void removeLocal(Object key) throws Exception {
		if (localCache != null)
			$async(() -> localCache.remove(key));
	}

	public void removeRemote(Object key) throws Exception {
		if (remoteCache != null)
			$async(() -> remoteCache.remove(key));
	}

	private void $async(NAFunction naf) {
		Single.create(new SingleOnSubscribe<NAFunction>() {
			@Override
			public void subscribe(SingleEmitter<NAFunction> e) throws Exception {
				e.onSuccess(naf);
			}
		}).subscribe(cacheObserver);
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
