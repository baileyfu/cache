package xcache;

import xcache.em.TimeUnit;

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
	public void put(int dbIndex, K key, V value) throws Exception;

	/** 加入缓存，并设定过期时间 */
	public void put(int dbIndex, K key, V value, long expiring, TimeUnit timeUnit) throws Exception;

	/** 从缓存删除 */
	public void remove(int dbIndex, K key) throws Exception;

	/** 从缓存读取 */
	public V get(int dbIndex, K key) throws Exception;

	/** 是否存在 */
	public boolean exists(int dbIndex, K key);

	/** 缓存大小 */
	public int size(int dbIndex);

	/** 清空缓存 */
	public void clear(int dbIndex) throws Exception;
}
