package com.lz.components.cache.core;

import com.lz.components.cache.LocalCache;
import com.lz.components.cache.RemoteCache;
import com.lz.components.cache.ShardCache;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.exception.LzRuntimeException;
import com.lz.components.common.log.holder.CommonLoggerHolder;
import com.lz.components.common.util.AsyncInvoker;
import com.lz.components.common.util.function.NAFunction;

/**
 * 缓存管理器<br/>
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-12 16:00
 */
public class CacheManager implements CommonLoggerHolder{
	protected final LocalCache localCache;
	private final ShardCache<Object, Object> localShardCache;
	protected final RemoteCache remoteCache;
	private final ShardCache<Object, Object> remoteShardCache;

	private AsyncInvoker asyncInvoker;
	protected boolean useable;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	CacheManager(LocalCache localCache, RemoteCache remoteCache) {
		asyncInvoker = new AsyncInvoker();
		this.useable = true;
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
	}
	public boolean useable(){
		return useable;
	}
	public void enable() {
		this.useable = true;
	}
	public void disable() {
		this.useable = false;
	}

	private boolean ifLocalUseable() {
		return useable && localCache.useable();
	}
	private boolean ifRemoteUseable() {
		return useable && remoteCache.useable();
	}
	/// -------------GET--------------//
	public Object getLocal(Object key) throws LzRuntimeException {
		return !ifLocalUseable() || key == null || localCache == null ? null : localCache.get(key);
	}
	public Object getLocal(String shardName, Object key) throws LzRuntimeException {
		return !ifLocalUseable() || key == null ? null : (localShardCache == null ? getLocal(key) : localShardCache.get(shardName, key));
	}

	public Object getRemote(Object key) throws LzRuntimeException {
		return !ifRemoteUseable() || key == null || remoteCache == null ? null : remoteCache.get(key);
	}
	public Object getRemote(String shardName, Object key) throws LzRuntimeException {
		return !ifRemoteUseable() || key == null ? null : (remoteShardCache == null ? getRemote(key) : remoteShardCache.get(shardName, key));
	}

	/// -------------PUT2Local--------------//
	public void putToLocal(Object key, Object value) throws LzRuntimeException {
		putToLocal(key,value,false);
	}
	public void putToLocal(Object key, Object value,boolean async) throws LzRuntimeException {
		if(!ifLocalUseable()||key==null||value==null)return;
		if (localCache != null)
			$update(() -> localCache.put(key, value),async);
	}
	public void putToLocal(String shardName, Object key, Object value) throws LzRuntimeException {
		putToLocal(shardName,key,value,false);
	}
	public void putToLocal(String shardName, Object key, Object value, boolean async) throws LzRuntimeException {
		if(!ifLocalUseable()||key==null||value==null)return;
		if (localShardCache == null)
			putToLocal(key, value,async);
		else
			$update(() -> localShardCache.put(shardName, key, value), async);
	}
	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit) throws LzRuntimeException {
		putToLocal(key, value, expiring, timeUnit, false);
	}
	public void putToLocal(Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws LzRuntimeException {
		if(!ifLocalUseable()||key==null||value==null)return;
		if (localCache != null)
			$update(() -> localCache.put(key, value, expiring, timeUnit),async);
	}
	public void putToLocal(String shardName,Object key, Object value, int expiring, TimeUnit timeUnit) throws LzRuntimeException {
		putToLocal(shardName,key,value,expiring,timeUnit,false);
	}
	public void putToLocal(String shardName,Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws LzRuntimeException {
		if(!ifLocalUseable()||key==null||value==null)return;
		if (localShardCache == null)
			putToLocal(key, value, expiring, timeUnit,async);
		else
			$update(() -> localShardCache.put(shardName, key, value, expiring, timeUnit),async);
	}
	/// -------------PUT2Remote--------------//
	public void putToRemote(Object key, Object value) throws LzRuntimeException {
		putToRemote(key, value, false);
	}
	public void putToRemote(Object key, Object value, boolean async) throws LzRuntimeException {
		if(!ifRemoteUseable()||key==null||value==null)return;
		if (remoteCache != null)
			$update(() -> remoteCache.put(key, value),async);
	}
	public void putToRemote(String shardName,Object key, Object value) throws LzRuntimeException {
		putToRemote(shardName, key, value, false);
	}
	public void putToRemote(String shardName,Object key, Object value, boolean async) throws LzRuntimeException {
		if(!ifRemoteUseable()||key==null||value==null)return;
		if(remoteShardCache==null)
			putToRemote(key, value,async);
		else
			$update(() -> remoteShardCache.put(shardName, key, value), async);
	}
	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit) throws LzRuntimeException {
		putToRemote(key, value, expiring, timeUnit, false);
	}
	public void putToRemote(Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws LzRuntimeException {
		if(!ifRemoteUseable()||key==null||value==null)return;
		if (remoteCache != null)
			$update(() -> remoteCache.put(key, value, expiring, timeUnit),async);
	}
	public void putToRemote(String shardName, Object key, Object value, int expiring, TimeUnit timeUnit) throws LzRuntimeException {
		putToRemote(shardName, key, value, expiring, timeUnit, false);
	}
	public void putToRemote(String shardName, Object key, Object value, int expiring, TimeUnit timeUnit, boolean async) throws LzRuntimeException {
		if(!ifRemoteUseable()||key==null||value==null)return;
		if (remoteShardCache == null)
			putToRemote(key, value, expiring, timeUnit,async);
		else
			$update(() -> remoteShardCache.put(shardName, key, value, expiring, timeUnit),async);
	}

	/// -------------REMOVE4Local--------------//
	public void removeLocal(Object key) throws LzRuntimeException {
		removeLocal(key, false);
	}
	public void removeLocal(Object key, boolean async) throws LzRuntimeException {
		if(!ifLocalUseable()||key==null)return;
		if (localCache != null)
			$update(() -> localCache.remove(key),async);
	}
	public void removeLocal(String shardName, Object key) throws LzRuntimeException {
		removeLocal(shardName, key);
	}
	public void removeLocal(String shardName, Object key, boolean async) throws LzRuntimeException {
		if(!ifLocalUseable()||key==null)return;
		if (localShardCache == null)
			removeLocal(key,async);
		else
			$update(() -> localShardCache.remove(shardName, key),async);
	}
	/// -------------REMOVE4Remote--------------//
	public void removeRemote(Object key) throws LzRuntimeException {
		removeRemote(key, false);
	}
	public void removeRemote(Object key, boolean async) throws LzRuntimeException {
		if(!ifRemoteUseable()||key==null)return;
		if (remoteCache != null)
			$update(() -> remoteCache.remove(key),async);
	}
	public void removeRemote(String shardName, Object key) throws LzRuntimeException {
		removeRemote(shardName, key, false);
	}
	public void removeRemote(String shardName, Object key, boolean async) throws LzRuntimeException {
		if(!ifRemoteUseable()||key==null)return;
		if (remoteShardCache == null)
			removeRemote(key,async);
		else
			$update(() -> remoteShardCache.remove(shardName, key),async);
	}

	private void $update(NAFunction naf,boolean async) throws LzRuntimeException{
		if(async){
			asyncInvoker.execute(naf);
		}else{
			naf.apply();
		}
	}
}
