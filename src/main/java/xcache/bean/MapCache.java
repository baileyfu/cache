package xcache.bean;

import java.util.Map;

import xcache.LocalCache;

public interface MapCache<K, V> extends LocalCache<K, V> {

	@Override
	default void put(K key, V value) throws Exception {
		value().put(key, value);
	}

	@Override
	default void remove(K key) throws Exception {
		value().remove(key);
	}

	@Override
	default V get(K key) throws Exception {
		return value().get(key);
	}

	@Override
	default boolean exists(K key) {
		return value().containsKey(key);
	}

	@Override
	default int size() {
		return value().size();
	}

	@Override
	default void clear() throws Exception {
		value().clear();
	}

	Map<K, V> value();

}
