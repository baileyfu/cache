package com.lz.components.cache.redis;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.RemoteCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.common.exception.LzRuntimeException;

public class SingleRedisCache extends RemoteCache {
	protected RedisTemplate<Object, Object> redisTemplate;

	public SingleRedisCache(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	protected void doRemove(Object key) throws LzRuntimeException {
		redisTemplate.delete(key);
	}

	@Override
	public JSONObject size() {
		JSONObject size = super.size();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.dbSize();
			}
		}).intValue());
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	public void clear() throws LzRuntimeException {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	@Override
	protected void doPut(Object key, Object value, long expiring, com.lz.components.cache.em.TimeUnit timeUnit)
			throws LzRuntimeException {
		if (expiring > 0) {
			BoundValueOperations<Object, Object> boundValue = redisTemplate.boundValueOps(key);
			boundValue.set(value);
			boundValue.expire(expiring, timeUnit.toConcurrent());
		} else {
			redisTemplate.boundValueOps(key).set(value);
		}
	}

	@Override
	protected Object doGet(Object key) {
		BoundValueOperations<Object, Object> boundValue = redisTemplate.boundValueOps(key);
		return boundValue != null ? boundValue.get() : null;
	}

	@Override
	public boolean exists(Object key) {
		return redisTemplate.hasKey(key);
	}
}
