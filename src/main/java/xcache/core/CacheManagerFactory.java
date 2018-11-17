package com.lz.components.cache.core;

import java.util.HashMap;
import java.util.Map;

import com.lz.components.cache.Cache;
import com.lz.components.cache.LocalCache;
import com.lz.components.cache.RemoteCache;

/**
 * 缓存管理器工厂
 * 
 * @author fuli
 * @date 2018年9月5日
 */
public class CacheManagerFactory {
	public static final String DEFAULT_CACHE_MANAGER_NAME = "DEFAULT_CACHE_MANAGER";
	private static Map<String, CacheManager> MANAGER_HOLDER = new HashMap<>();
	private String clazz;
	private Map<String,Cache<Object,Object>[]> batchMaterials;
	private Cache<Object,Object>[] singleMaterial;
	
	public CacheManagerFactory(){
		this.clazz = CacheManager.class.getSimpleName();
	}
	public CacheManagerFactory(String clazz){
		this.clazz = clazz;
	}
	
	public void batchCreate() {
		if (singleMaterial != null) {
			this.create((LocalCache)singleMaterial[0], (RemoteCache)singleMaterial[1]);
		}
		if (batchMaterials != null) {
			for (String cacheName : batchMaterials.keySet()) {
				Cache<Object, Object>[] cacheArray = batchMaterials.get(cacheName);
				create(cacheName, (LocalCache) cacheArray[0], (RemoteCache) cacheArray[1]);
			}
		}
	}
	
	public void create(LocalCache localCache) {
		createAndSave(DEFAULT_CACHE_MANAGER_NAME,localCache,null);
	}
	public void create(String cacheManagerName,LocalCache localCache) {
		createAndSave(cacheManagerName,localCache,null);
	}
	public void create(LocalCache localCache, RemoteCache remoteCache) {
		createAndSave(DEFAULT_CACHE_MANAGER_NAME,localCache,remoteCache);
	}
	public void create(String cacheManagerName,LocalCache localCache, RemoteCache remoteCache) {
		createAndSave(cacheManagerName,localCache,remoteCache);
	}
	
	private CacheManager createAndSave(String cacheManagerName, LocalCache localCache, RemoteCache remoteCache) {
		CacheManager cacheManager = MANAGER_HOLDER.get(cacheManagerName);
		if (cacheManager == null) {
			localCache.setName(cacheManagerName);
			remoteCache.setName(cacheManagerName);
			if (clazz.equals(CustomCacheManager.class.getSimpleName())) {
				cacheManager = new CustomCacheManager(localCache, remoteCache);
			} else {
				cacheManager = new CacheManager(localCache, remoteCache);
			}
			MANAGER_HOLDER.put(cacheManagerName, cacheManager);
		}
		return cacheManager;
	}
	
	public void setBatchMaterials(Map<String, Cache<Object, Object>[]> batchMaterials) {
		this.batchMaterials = batchMaterials;
	}

	public void setSingleMaterial(Cache<Object, Object>[] singleMaterial) {
		this.singleMaterial = singleMaterial;
	}

	@SuppressWarnings("unchecked")
	public static <T extends CacheManager>T get() {
		return (T)get(DEFAULT_CACHE_MANAGER_NAME);
	}
	@SuppressWarnings("unchecked")
	public static <T extends CacheManager>T get(String cacheManagerName) {
		return (T)MANAGER_HOLDER.get(cacheManagerName);
	}
}
