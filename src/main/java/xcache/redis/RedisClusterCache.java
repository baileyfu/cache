package com.lz.components.cache.redis;

import com.lz.components.cache.RemoteCache;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 基于Redis集群方式的实现
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-06 10:42
 */
public class RedisClusterCache extends RemoteCache {
	@Override
	public boolean exists(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() throws LzRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	protected Object doGet(Object key) throws LzRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doPut(Object key, Object value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doRemove(Object key) throws LzRuntimeException {
		// TODO Auto-generated method stub
		
	}

}
