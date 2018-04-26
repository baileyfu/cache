package xcache.redis;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import xcache.RemoteCache;

public class SingleRedisCache implements RemoteCache {
	protected RedisTemplate<Object, Object> redisTemplate;

	public SingleRedisCache(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void remove(Object key) throws Exception {
		redisTemplate.delete(key);
	}

	@Override
	public int size() {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.dbSize();
			}
		}).intValue();
	}

	@Override
	public void clear() throws Exception {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	@Override
	public void put(Object key, Object value) throws Exception {
		redisTemplate.boundValueOps(key).set(value);
	}

	@Override
	public void put(Object key, Object value, long expiring, xcache.em.TimeUnit timeUnit) throws Exception {
		BoundValueOperations<Object, Object> boundValue = redisTemplate.boundValueOps(key);
		boundValue.set(value);
		boundValue.expire(expiring, timeUnit.toConcurrent());
	}

	@Override
	public Object get(Object key) {
		BoundValueOperations<Object, Object> boundValue = redisTemplate.boundValueOps(key);
		return boundValue != null ? boundValue.get() : null;
	}

	@Override
	public boolean exists(Object key) {
		return redisTemplate.hasKey(key);
	}
}
