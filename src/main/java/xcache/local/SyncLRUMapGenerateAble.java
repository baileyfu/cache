package com.lz.components.cache.local;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

@SuppressWarnings({ "unchecked", "rawtypes" })
public interface SyncLRUMapGenerateAble {
	// 默认size为1000
	int DEFAULT_SIZE = 1000;

	default public Map<Object,Object> generateLRUMap() {
		return Collections.synchronizedMap(new LRUMap(DEFAULT_SIZE));
	}

	default public Map<Object,Object> generateLRUMap(int size) {
		return Collections.synchronizedMap(new LRUMap(size < 1 ? DEFAULT_SIZE : size));
	}

	default public Map<Object,Object> generateLRUMap(Map map) {
		return Collections.synchronizedMap(new LRUMap(map));
	}
}
