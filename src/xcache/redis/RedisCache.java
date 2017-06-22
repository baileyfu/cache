package xcache.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import xcache.RemoteCache;

public class RedisCache implements RemoteCache {
	private RedisTemplate<Object, Object> redisTemplate;

	public RedisCache(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void remove(Object key) throws Exception {
		redisTemplate.delete(key);
	}

	@Override
	public int size() {
		return redisTemplate.keys("*").size();
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
		boundValue.expire(expiring, convert(timeUnit));
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

	private TimeUnit convert(xcache.em.TimeUnit timeUnit) {
		return timeUnit == xcache.em.TimeUnit.DAY ? TimeUnit.DAYS
				: timeUnit == xcache.em.TimeUnit.HOUR ? TimeUnit.HOURS
						: timeUnit == xcache.em.TimeUnit.MINUTE ? TimeUnit.MINUTES
								: timeUnit == xcache.em.TimeUnit.SECOND ? TimeUnit.SECONDS : null;
	}
}
