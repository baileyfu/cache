package com.lz.components.cache.aspect;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import com.lz.components.cache.annotation.LCache;
import com.lz.components.cache.annotation.RCache;
import com.lz.components.cache.annotation.XCache;
import com.lz.components.cache.em.CacheType;
import com.lz.components.cache.em.TimeUnit;

public class AnnoBean implements Cloneable{
	CacheType cacheType;
	String cacheName;
	String shardName;
	String key;
	String[] remove;
	boolean throwable;
	int expiring;
	TimeUnit timeUnit;
	String prefix;
	String suffix;

	public AnnoBean() {
		cacheName = StringUtils.EMPTY;
		shardName = StringUtils.EMPTY;
		key = StringUtils.EMPTY;
		timeUnit = TimeUnit.SECOND;
		prefix = StringUtils.EMPTY;
		suffix = StringUtils.EMPTY;
	}
	boolean isLocal() {
		return cacheType == CacheType.LOCAL;
	}

	boolean isRemote() {
		return cacheType == CacheType.REMOTE;
	}

	public static AnnoBean toAnnoBean(Annotation cacheAnno,final AnnoBean xAnnoBean){
		AnnoBean ab = null;
		try {
			ab = xAnnoBean == null ? new AnnoBean() : (AnnoBean) xAnnoBean.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		if (cacheAnno instanceof XCache) {
			XCache xCache = (XCache) cacheAnno;
			ab.cacheName=xCache.cacheName();
			ab.shardName = xCache.shardName();
			ab.key = xCache.key();
			ab.remove = xCache.remove();
			ab.expiring = xCache.expiring();
			ab.timeUnit = xCache.timeUnit();
			ab.prefix = xCache.prefix();
			ab.suffix = xCache.suffix();
		} else if (cacheAnno instanceof RCache) {
			RCache rCache = (RCache) cacheAnno;
			ab.cacheType = CacheType.REMOTE;
			ab.cacheName=rCache.cacheName();
			ab.shardName=rCache.shardName();
			ab.key = StringUtils.isNotBlank(rCache.key()) ? rCache.key() : ab.key;
			ab.remove = unique(ab.remove, rCache.remove());
			ab.throwable = rCache.throwable();
			ab.expiring = rCache.expiring() > 0 ? rCache.expiring() : ab.expiring;
			ab.timeUnit = rCache.timeUnit() != TimeUnit.NULL ? rCache.timeUnit() : (ab.timeUnit != TimeUnit.NULL ? ab.timeUnit : TimeUnit.MINUTE);
			ab.prefix = StringUtils.isNotBlank(rCache.prefix()) ? rCache.prefix() : ab.prefix;
			ab.suffix = StringUtils.isNotBlank(rCache.suffix()) ? rCache.suffix() : ab.suffix;
		} else if (cacheAnno instanceof LCache) {
			LCache lCache = (LCache) cacheAnno;
			ab.cacheType = CacheType.LOCAL;
			ab.cacheName=lCache.cacheName();
			ab.shardName = lCache.shardName();
			ab.key = StringUtils.isNotBlank(lCache.key()) ? lCache.key() : ab.key;
			ab.remove = unique(ab.remove, lCache.remove());
			ab.throwable = lCache.throwable();
			ab.expiring = lCache.expiring() > 0 ? lCache.expiring() : ab.expiring;
			ab.timeUnit = lCache.timeUnit() != TimeUnit.NULL ? lCache.timeUnit() : (ab.timeUnit != TimeUnit.NULL ? ab.timeUnit : TimeUnit.MINUTE);
			ab.prefix = StringUtils.isNotBlank(lCache.prefix()) ? lCache.prefix() : ab.prefix;
			ab.suffix = StringUtils.isNotBlank(lCache.suffix()) ? lCache.suffix() : ab.suffix;
		}else{
			ab = null;
		}
		return ab;
	}
	private static String[] unique(String[] sa1, String[] sa2) {
		if (sa1 == null) {
			return sa2;
		}
		if (sa2 == null) {
			return sa1;
		}
		HashSet<String> hs = new HashSet<>();
		for (String s : sa1) {
			if (StringUtils.isNotBlank(s))
				hs.add(s);
		}
		for (String s : sa2) {
			if (StringUtils.isNotBlank(s))
				hs.add(s);
		}
		return hs.toArray(new String[hs.size()]);
	}

	@Override
	public String toString() {
		return "AnnoBean [cacheType=" + cacheType + ", cacheName=" + cacheName + ", shardName=" + shardName + ", key="
				+ key + ", remove=" + Arrays.toString(remove) + ", throwable=" + throwable + ", expiring=" + expiring
				+ ", timeUnit=" + timeUnit + ", prefix=" + prefix + ", suffix=" + suffix + "]";
	}
}
