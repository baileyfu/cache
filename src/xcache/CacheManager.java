package xcache;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commons.fun.NAFunction;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.schedulers.Schedulers;
import xcache.em.TimeUnit;

/**
 * 缓存管理器
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 16:00
 */
public final class CacheManager {
	public static Logger logger = LoggerFactory.getLogger(CacheManager.class);
	private static CacheManager cacheManager = null;

	private final LocalCache<Object, Object> localCache;
	private final ShardCache<Object, Object> localShardCache;
	private final RemoteCache remoteCache;
	private final ShardCache<Object, Object> remoteShardCache;

	private FlowableEmitter<NAFunction> emitter;
	private Subscription subscription;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CacheManager(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		this.localCache = localCache;
		this.remoteCache = remoteCache;
		if (this.localCache == null) {
			logger.warn("No LocalCache be found !");
		}
		if (this.remoteCache == null) {
			logger.warn("No RemoteCache be found !");
		}
		localShardCache = (localCache != null && localCache instanceof ShardCache) ? (ShardCache) localCache : null;
		remoteShardCache = (remoteCache != null && remoteCache instanceof ShardCache) ? (ShardCache) remoteCache : null;
		Flowable.<NAFunction>create((emitter) -> this.emitter = emitter, BackpressureStrategy.BUFFER)
				.observeOn(Schedulers.io())
				.subscribe(new Subscriber<NAFunction>() {
					@Override
					public void onComplete() {
					}
					@Override
					public void onError(Throwable e) {
						logger.error("XCache error !", e);
					}
					@Override
					public void onNext(NAFunction naf) {
						try {
							naf.apply();
						} catch (Exception e) {
							logger.error("XCache putting error !", e);
						}
					}
					@Override
					public void onSubscribe(Subscription sub) {
						subscription = sub;
					}
				});
	}
	public Object getLocal(Object key) throws Exception {
		return localCache == null ? null : localCache.get(key);
	}
	public Object getLocal(String shardName, Object key) throws Exception {
		return localShardCache == null ? getLocal(key) : localShardCache.get(shardName, key);
	}

	public Object getRemote(Object key) throws Exception {
		return remoteCache == null ? null : remoteCache.get(key);
	}
	public Object getRemote(String shardName, Object key) throws Exception {
		return remoteShardCache == null ? getRemote(key) : remoteShardCache.get(shardName, key);
	}

	public void putToLocal(Object key, Object value) throws Exception {
		if (localCache != null)
			$async(() -> localCache.put(key, value));
	}
	public void putToLocal(String shardName, Object key, Object value) throws Exception {
		if (localShardCache == null)
			putToLocal(key, value);
		else
			$async(() -> localShardCache.put(shardName, key, value));
	}
	
	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		if (localCache != null)
			$async(() -> localCache.put(key, value, expiring, timeUnit));
	}
	public void putToLocal(String shardName,Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		if (localShardCache == null)
			putToLocal(key, value, expiring, timeUnit);
		else
			$async(() -> localShardCache.put(shardName, key, value, expiring, timeUnit));
	}

	public void putToRemote(Object key, Object value) throws Exception {
		if (remoteCache != null)
			$async(() -> remoteCache.put(key, value));
	}
	public void putToRemote(String shardName,Object key, Object value) throws Exception {
		if(remoteShardCache==null)
			putToRemote(key, value);
		else
			$async(() -> remoteShardCache.put(shardName, key, value));
	}

	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		if (remoteCache != null)
			$async(() -> remoteCache.put(key, value, expiring, timeUnit));
	}
	public void putToRemote(String shardName, Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		if (remoteShardCache == null)
			putToRemote(key, value, expiring, timeUnit);
		else
			$async(() -> remoteShardCache.put(shardName, key, value, expiring, timeUnit));
	}

	public void removeLocal(Object key) throws Exception {
		if (localCache != null)
			$async(() -> localCache.remove(key));
	}
	public void removeLocal(String shardName, Object key) throws Exception {
		if (localShardCache == null)
			removeLocal(key);
		else
			$async(() -> localShardCache.remove(shardName, key));
	}

	public void removeRemote(Object key) throws Exception {
		if (remoteCache != null)
			$async(() -> remoteCache.remove(key));
	}
	public void removeRemote(String shardName, Object key) throws Exception {
		if (remoteShardCache == null)
			removeRemote(key);
		else
			$async(() -> remoteShardCache.remove(shardName, key));
	}

	private void $async(NAFunction naf) {
		emitter.onNext(naf);
		subscription.request(Long.MAX_VALUE);
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
