package xcache.aspect;

import java.lang.annotation.Annotation;

import xcache.annotation.LCache;
import xcache.annotation.RCache;
import xcache.em.CacheType;
import xcache.em.TimeUnit;

public class AnnoBean {
	CacheType cacheType;
	String key;
	String[] remove;
	boolean throwable;
	int expiring;
	TimeUnit timeUnit;
	String prefix;
	String suffix;

	boolean isLocal() {
		return cacheType == CacheType.LOCAL;
	}

	boolean isRemote() {
		return cacheType == CacheType.REMOTE;
	}

	public static AnnoBean toAnnoBean(Annotation cacheAnno) {
		AnnoBean ab = null;
		if (cacheAnno instanceof RCache) {
			RCache rCache = (RCache) cacheAnno;
			ab = new AnnoBean();
			ab.cacheType = CacheType.REMOTE;
			ab.key = rCache.key();
			ab.remove = rCache.remove();
			ab.throwable = rCache.throwable();
			ab.expiring = rCache.expiring();
			ab.timeUnit = rCache.timeUnit();
			ab.prefix = rCache.prefix();
			ab.suffix = rCache.suffix();
		} else if (cacheAnno instanceof LCache) {
			LCache lCache = (LCache) cacheAnno;
			ab = new AnnoBean();
			ab.cacheType = CacheType.LOCAL;
			ab.key = lCache.key();
			ab.remove = lCache.remove();
			ab.throwable = lCache.throwable();
			ab.expiring = lCache.expiring();
			ab.timeUnit = lCache.timeUnit();
			ab.prefix = lCache.prefix();
			ab.suffix = lCache.suffix();
		}
		return ab;
	}
}
