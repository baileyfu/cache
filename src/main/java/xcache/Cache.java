package com.lz.components.cache;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.cache.em.TimeUnit;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 缓存接口
 * 
 * @author bailey.fu
 * @date Dec 14, 2010
 * @version 1.0
 * @description
 */
public interface Cache<K, V> {

	/** 加入缓存 */
	public void put(K key, V value) throws LzRuntimeException;

	/** 加入缓存，并设定过期时间 */
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws LzRuntimeException;

	/** 从缓存删除 */
	public void remove(K key) throws LzRuntimeException;

	/** 从缓存读取 */
	public V get(K key) throws LzRuntimeException;

	/** 是否存在 */
	public boolean exists(K key);

	/** 缓存容量情况 */
	public JSONObject size();

	/** 清空缓存 */
	public void clear() throws LzRuntimeException;
	
	/** 是否开启 */
	public boolean useable();
	/** 缓存使用情况报告 */
	public JSONObject report();
	
	public String cacheName();
}
