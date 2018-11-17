package com.lz.components.cache;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 可选数据库下标
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-07-20 14:09
 * @param <K>
 * @param <V>
 */
public interface ShardCache<K, V> {
	/** 加入缓存 */
	public void put(String shardName, K key, V value) throws LzRuntimeException;

	/** 加入缓存，并设定过期时间 */
	public void put(String shardName, K key, V value, long expiring, TimeUnit timeUnit) throws LzRuntimeException;

	/** 从缓存删除 */
	public void remove(String shardName, K key) throws LzRuntimeException;

	/** 从缓存读取 */
	public V get(String shardName, K key) throws LzRuntimeException;

	/** 是否存在 */
	public boolean exists(String shardName, K key);

	/** 缓存大小 */
	public JSONObject size(String shardName);

	/** 清空缓存 */
	public void clear(String shardName) throws LzRuntimeException;
}
