package xcache.core;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import commons.fun.NAFunction;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.schedulers.Schedulers;
import xcache.LocalCache;
import xcache.RemoteCache;
import xcache.ShardCache;
import xcache.XcacheLoggerHolder;
import xcache.em.TimeUnit;

/**
 * 缓存管理器<br/>
 * 单例实现
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 16:00
 */
public final class CacheManager implements XcacheLoggerHolder{
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
			LOGGER.warn("No LocalCache be found !");
		}
		if (this.remoteCache == null) {
			LOGGER.warn("No RemoteCache be found !");
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
						LOGGER.error("XCache error !", e);
					}
					@Override
					public void onNext(NAFunction naf) {
						try {
							naf.apply();
						} catch (Exception e) {
							LOGGER.error("XCache putting error !", e);
						}
					}
					@Override
					public void onSubscribe(Subscription sub) {
						subscription = sub;
					}
				});
	}

	/// -------------GET--------------//
	public Object getLocal(Object key) throws Exception {
		return key == null || localCache == null ? null : localCache.get(key);
	}
	public Object getLocal(String shardName, Object key) throws Exception {
		return key == null ? null : (localShardCache == null ? getLocal(key) : localShardCache.get(shardName, key));
	}

	public Object getRemote(Object key) throws Exception {
		return key == null || remoteCache == null ? null : remoteCache.get(key);
	}
	public Object getRemote(String shardName, Object key) throws Exception {
		return key == null ? null : (remoteShardCache == null ? getRemote(key) : remoteShardCache.get(shardName, key));
	}

	/// -------------PUT2Local--------------//
	public void putToLocal(Object key, Object value) throws Exception {
		putToLocal(key,value,false);
	}
	public void putToLocal(Object key, Object value,boolean async) throws Exception {
		if(key==null||value==null)return;
		if (localCache != null)
			$update(() -> localCache.put(key, value),async);
	}
	public void putToLocal(String shardName, Object key, Object value) throws Exception {
		putToLocal(shardName,key,value,false);
	}
	public void putToLocal(String shardName, Object key, Object value, boolean async) throws Exception {
		if(key==null||value==null)return;
		if (localShardCache == null)
			putToLocal(key, value,async);
		else
			$update(() -> localShardCache.put(shardName, key, value), async);
	}
	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		putToLocal(key, value, expiring, timeUnit, false);
	}
	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws Exception {
		if(key==null||value==null)return;
		if (localCache != null)
			$update(() -> localCache.put(key, value, expiring, timeUnit),async);
	}
	public void putToLocal(String shardName,Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		putToLocal(shardName,key,value,expiring,timeUnit,false);
	}
	public void putToLocal(String shardName,Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws Exception {
		if(key==null||value==null)return;
		if (localShardCache == null)
			putToLocal(key, value, expiring, timeUnit,async);
		else
			$update(() -> localShardCache.put(shardName, key, value, expiring, timeUnit),async);
	}
	/// -------------PUT2Remote--------------//
	public void putToRemote(Object key, Object value) throws Exception {
		putToRemote(key, value, false);
	}
	public void putToRemote(Object key, Object value, boolean async) throws Exception {
		if(key==null||value==null)return;
		if (remoteCache != null)
			$update(() -> remoteCache.put(key, value),async);
	}
	public void putToRemote(String shardName,Object key, Object value) throws Exception {
		putToRemote(shardName, key, value, false);
	}
	public void putToRemote(String shardName,Object key, Object value, boolean async) throws Exception {
		if(key==null||value==null)return;
		if(remoteShardCache==null)
			putToRemote(key, value,async);
		else
			$update(() -> remoteShardCache.put(shardName, key, value), async);
	}
	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		putToRemote(key, value, expiring, timeUnit, false);
	}
	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws Exception {
		if(key==null||value==null)return;
		if (remoteCache != null)
			$update(() -> remoteCache.put(key, value, expiring, timeUnit),async);
	}
	public void putToRemote(String shardName, Object key, Object value, int expiring, TimeUnit timeUnit) throws Exception {
		putToRemote(shardName, key, value, expiring, timeUnit, false);
	}
	public void putToRemote(String shardName, Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws Exception {
		if(key==null||value==null)return;
		if (remoteShardCache == null)
			putToRemote(key, value, expiring, timeUnit,async);
		else
			$update(() -> remoteShardCache.put(shardName, key, value, expiring, timeUnit),async);
	}

	/// -------------REMOVE4Local--------------//
	public void removeLocal(Object key) throws Exception {
		removeLocal(key, false);
	}
	public void removeLocal(Object key, boolean async) throws Exception {
		if(key==null)return;
		if (localCache != null)
			$update(() -> localCache.remove(key),async);
	}
	public void removeLocal(String shardName, Object key) throws Exception {
		removeLocal(shardName, key);
	}
	public void removeLocal(String shardName, Object key, boolean async) throws Exception {
		if(key==null)return;
		if (localShardCache == null)
			removeLocal(key,async);
		else
			$update(() -> localShardCache.remove(shardName, key),async);
	}
	/// -------------REMOVE4Remote--------------//
	public void removeRemote(Object key) throws Exception {
		removeRemote(key, false);
	}
	public void removeRemote(Object key, boolean async) throws Exception {
		if(key==null)return;
		if (remoteCache != null)
			$update(() -> remoteCache.remove(key),async);
	}
	public void removeRemote(String shardName, Object key) throws Exception {
		removeRemote(shardName, key, false);
	}
	public void removeRemote(String shardName, Object key, boolean async) throws Exception {
		if(key==null)return;
		if (remoteShardCache == null)
			removeRemote(key,async);
		else
			$update(() -> remoteShardCache.remove(shardName, key),async);
	}

	private void $update(NAFunction naf,boolean async) throws Exception{
		if(async){
			emitter.onNext(naf);
			subscription.request(Long.MAX_VALUE);
		}else{
			naf.apply();
		}
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

	public static synchronized CacheManager create(LocalCache<Object, Object> localCache) {
		if (cacheManager == null) {
			syncInit(localCache, null);
		}
		return cacheManager;
	}

	public static synchronized CacheManager create(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		if (cacheManager == null) {
			syncInit(localCache, remoteCache);
		}
		return cacheManager;
	}
}
