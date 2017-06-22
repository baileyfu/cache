package xcache.spring;

import xcache.CacheManager;
import xcache.LocalCache;
import xcache.RemoteCache;

public class CacheConfiguration {
	public CacheConfiguration(LocalCache<Object, Object> localCache) {
		CacheManager.create(localCache);
	}

	public CacheConfiguration(LocalCache<Object, Object> localCache, RemoteCache remoteCache) {
		CacheManager.create(localCache, remoteCache);
	}
}
