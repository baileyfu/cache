package xcache;

import xcache.em.TimeUnit;

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
	public void put(K key, V value) throws Exception;

	/** 加入缓存，并设定过期时间 */
	public void put(K key, V value, long expiring, TimeUnit timeUnit) throws Exception;

	/** 从缓存删除 */
	public void remove(K key) throws Exception;

	/** 从缓存读取 */
	public V get(K key) throws Exception;

	/** 是否存在 */
	public boolean exists(K key);

	/** 缓存大小 */
	public int size();

	/** 清空缓存 */
	public void clear() throws Exception;
}
