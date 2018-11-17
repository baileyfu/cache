package com.lz.components.cache.local;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.LocalCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.exception.LzRuntimeException;

public abstract class MapCache extends LocalCache {

	@Override
	protected void doPut(Object key, Object value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		value().put(key, value);
	}

	@Override
	protected void doRemove(Object key) throws LzRuntimeException {
		value().remove(key);
	}

	@Override
	protected Object doGet(Object key) throws LzRuntimeException {
		return value().get(key);
	}

	@Override
	public boolean exists(Object key) {
		return value().containsKey(key);
	}

	@Override
	public void clear() throws LzRuntimeException {
		value().clear();
	}
	@Override
	public JSONObject size() {
		JSONObject size = super.size();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		Map<Object, Object> map = value();
		size.put(CacheParams.SIZE_QUANTITY.NAME, map == null ? -1 : map.size());
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}
	protected abstract Map<Object, Object> value();
}
