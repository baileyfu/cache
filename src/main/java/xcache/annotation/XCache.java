package com.lz.components.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

import com.lz.components.cache.core.CacheManagerFactory;
import com.lz.components.cache.em.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XCache {
	String cacheName() default CacheManagerFactory.DEFAULT_CACHE_MANAGER_NAME;

	String shardName() default StringUtils.EMPTY;

	String key() default StringUtils.EMPTY;

	String[] remove() default StringUtils.EMPTY;
	/** cache up till satisfied with condition */
	String till() default StringUtils.EMPTY;
	/** can not remove untill satisfied with condition */
	String untill() default StringUtils.EMPTY;
	
	String ifRemove() default StringUtils.EMPTY;
	
	int expiring() default 0;

	TimeUnit timeUnit() default TimeUnit.NULL;

	String prefix() default StringUtils.EMPTY;

	String suffix() default StringUtils.EMPTY;
}
