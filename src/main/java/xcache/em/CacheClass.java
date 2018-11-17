package com.lz.components.cache.em;

import org.apache.commons.lang3.StringUtils;

import com.lz.components.cache.local.EHCache;
import com.lz.components.cache.local.map.LRUMapCache;
import com.lz.components.cache.local.map.MultiLRUMapCache;
import com.lz.components.cache.local.map.MultiMapCache;
import com.lz.components.cache.local.map.SingleMapAutoCleanCache;
import com.lz.components.cache.local.map.SingleMapCache;
import com.lz.components.cache.local.map.shard.LRUMapShardCache;
import com.lz.components.cache.local.map.shard.MultiLRUMapShardCache;
import com.lz.components.cache.local.map.shard.MultiMapAutoCleanShardCache;
import com.lz.components.cache.local.map.shard.MultiMapShardCache;
import com.lz.components.cache.local.map.shard.SingleMapAutoCleanShardCache;
import com.lz.components.cache.local.map.shard.SingleMapShardCache;
import com.lz.components.cache.local.shard.EHShardCache;
import com.lz.components.cache.redis.SingleRedisCache;
import com.lz.components.cache.redis.shard.SingleRedisShardCache;
import com.lz.components.common.beanutil.ReflectionUtils;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * Cache的实现类描述
 * 
 * @author fuli
 * @date 2018年9月6日
 * @version 1.0.0
 */
public enum CacheClass {
	/*** Local ***/
	EHCache(EHCache.class), LRUMapCache(LRUMapCache.class), MultiLRUMapCache(MultiLRUMapCache.class), MultiMapAutoCleanCache(MultiLRUMapCache.class), MultiMapCache(MultiMapCache.class), SingleMapAutoCleanCache(SingleMapAutoCleanCache.class), SingleMapCache(SingleMapCache.class),
	// shard cache
	EHShardCache(EHShardCache.class), LRUMapShardCache(LRUMapShardCache.class), MultiLRUMapShardCache(MultiLRUMapShardCache.class), MultiMapAutoCleanShardCache(MultiMapAutoCleanShardCache.class), MultiMapShardCache(MultiMapShardCache.class), SingleMapAutoCleanShardCache(SingleMapAutoCleanShardCache.class), SingleMapShardCache(SingleMapShardCache.class),
	/*** Remote ***/
	SingleRedisCache(SingleRedisCache.class),
	// shard cache
	SingleRedisShardCache(SingleRedisShardCache.class);
	private Class<?> clazz;
	private CacheClass(Class<?> clazz){
		this.clazz=clazz;
	}
	/**
	 * 
	 * @param args 仅一个参数且为空字符串时,则认为args为null
	 * @return
	 */
	public <T> T instantiates(Object[] args) {
		try {
			return ReflectionUtils.getInstance(clazz,
					args == null || (args.length == 1 && StringUtils.isBlank(args[0].toString())) ? null : args);
		} catch (Exception e) {
			throw new LzRuntimeException(e);
		}
	}
}
