package xcache.redis;

import xcache.RemoteCache;
import xcache.em.TimeUnit;

/**
 * 基于Redis集群方式的实现
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-06 10:42
 */
public class RedisClusterCache implements RemoteCache {

	@Override
	public void put(Object key, Object value) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void put(Object key, Object value, long expiring, TimeUnit timeUnit) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(Object key) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Object get(Object key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() throws Exception {
		// TODO Auto-generated method stub

	}

}
