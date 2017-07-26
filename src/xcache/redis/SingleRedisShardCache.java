package xcache.redis;

import java.util.Set;
import java.util.function.BiFunction;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.RedisSerializer;

import xcache.ShardCache;
import xcache.em.TimeUnit;

/**
 * 可指定数据库下标
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-07-20 13:41
 */
public class SingleRedisShardCache extends SingleRedisCache implements ShardCache<Object, Object> {

	public SingleRedisShardCache(RedisTemplate<Object, Object> redisTemplate) {
		super(redisTemplate);
	}

	@Override
	public void put(int dbIndex, Object key, Object value) throws Exception {
		put(dbIndex, key, value, -1, null);
	}

	@Override
	public void put(int dbIndex, Object key, Object value, long expiring, TimeUnit timeUnit) throws Exception {
		checkIndex(dbIndex);
		redisTemplate.execute(new RedisCallback<Object>() {
			@SuppressWarnings("unchecked")
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				byte[] keyBytes = serialize.apply((RedisSerializer<Object>) redisTemplate.getKeySerializer(), key);
				byte[] valueBytes = serialize.apply((RedisSerializer<Object>) redisTemplate.getValueSerializer(), value);
				connection.set(keyBytes, valueBytes);
				if (expiring > 0)
					connection.pExpire(keyBytes, TimeoutUtils.toMillis(expiring, timeUnit.toConcurrent()));
				return value;
			}
		});
	}

	@Override
	public void remove(int dbIndex, Object key) throws Exception {
		checkIndex(dbIndex);
		redisTemplate.execute(new RedisCallback<Long>() {
			@SuppressWarnings("unchecked")
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				return connection.del(serialize.apply((RedisSerializer<Object>) redisTemplate.getKeySerializer(), key));
			}
		});
	}

	@Override
	public Object get(int dbIndex, Object key) throws Exception {
		checkIndex(dbIndex);
		return redisTemplate.execute(new RedisCallback<Object>() {
			@SuppressWarnings("unchecked")
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				byte[] valueBytes = connection.get(serialize.apply((RedisSerializer<Object>) redisTemplate.getKeySerializer(), key));
				return redisTemplate.getValueSerializer().deserialize(valueBytes);
			}
		});
	}

	@Override
	public boolean exists(int dbIndex, Object key) {
		checkIndex(dbIndex);
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@SuppressWarnings("unchecked")
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				return connection.exists(serialize.apply((RedisSerializer<Object>) redisTemplate.getKeySerializer(), key));
			}
		});
	}

	@Override
	public int size(int dbIndex) {
		checkIndex(dbIndex);
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				return connection.dbSize();
			}
		}).intValue();
	}

	@Override
	public void clear(int dbIndex) throws Exception {
		checkIndex(dbIndex);
		redisTemplate.execute(new RedisCallback<Long>() {
			@SuppressWarnings("unchecked")
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				Set<byte[]> keys=connection.keys(serialize.apply((RedisSerializer<Object>) redisTemplate.getKeySerializer(), "*"));
				return connection.del(keys.toArray(new byte[keys.size()][]));
			}
		});
	}

	private BiFunction<RedisSerializer<Object>, Object, byte[]> serialize = (s, t) -> (t instanceof byte[]) ? (byte[]) t : s.serialize(t);

	private void checkIndex(int dbIndex) {
		if (dbIndex < 0 || dbIndex > 15) {
			throw new java.lang.IllegalArgumentException("Illegal dbIndex for Redis.It should between 0 to 15");
		}
	}
}