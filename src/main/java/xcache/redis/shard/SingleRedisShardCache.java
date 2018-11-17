package com.lz.components.cache.redis.shard;

import java.util.Set;
import java.util.function.BiFunction;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.Shard;
import com.lz.components.cache.ShardCache;
import com.lz.components.cache.em.CacheParams;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.cache.redis.RedisShard;
import com.lz.components.cache.redis.SingleRedisCache;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 可指定数据库下标
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-07-20 13:41
 */
public class SingleRedisShardCache extends SingleRedisCache implements ShardCache<Object, Object> {
	private Shard shard;
	public SingleRedisShardCache(RedisTemplate<Object, Object> redisTemplate) {
		super(redisTemplate);
		shard=new RedisShard();
	}

	@Override
	public void put(String shardName, Object key, Object value) throws LzRuntimeException {
		put(shardName, key, value, -1, null);
	}

	@Override
	public void put(String shardName, Object key, Object value, long expiring, TimeUnit timeUnit) throws LzRuntimeException {
		putCount++;
		int dbIndex=shard.convertToIndex(shardName);
		if(dbIndex==-1){
			super.put(key, value, expiring, timeUnit);
			return;
		}
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
	public void remove(String shardName, Object key) throws LzRuntimeException {
		removeCount++;
		int dbIndex=shard.convertToIndex(shardName);
		if(dbIndex==-1){
			super.remove(key);
			return;
		}
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
	public Object get(String shardName, Object key) throws LzRuntimeException {
		getCount++;
		int dbIndex=shard.convertToIndex(shardName);
		Object value = null;
		if(dbIndex==-1){
			value = super.get(key);
		} else {
			value = redisTemplate.execute(new RedisCallback<Object>() {
				@SuppressWarnings("unchecked")
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					connection.select(dbIndex);
					byte[] valueBytes = connection
							.get(serialize.apply((RedisSerializer<Object>) redisTemplate.getKeySerializer(), key));
					return redisTemplate.getValueSerializer().deserialize(valueBytes);
				}
			});
		}
		if (value != null) {
			hitCount++;
		}
		return value;
	}

	@Override
	public boolean exists(String shardName, Object key) {
		int dbIndex=shard.convertToIndex(shardName);
		if(dbIndex==-1){
			return super.exists(key);
		}
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
	public JSONObject size(String shardName) {
		int dbIndex = shard.convertToIndex(shardName);
		if (dbIndex == -1) {
			return super.size();
		}
		JSONObject size = new JSONObject();
		size.put(CacheParams.SIZE_CAPACITY.NAME, 0);
		size.put(CacheParams.SIZE_QUANTITY.NAME, redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				connection.select(dbIndex);
				return connection.dbSize();
			}
		}).intValue());
		size.put(CacheParams.SIZE_MEMORY.NAME, 0);
		return size;
	}

	@Override
	public void clear(String shardName) throws LzRuntimeException {
		int dbIndex=shard.convertToIndex(shardName);
		if (dbIndex == -1) {
			super.clear();
			return;
		}
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

}