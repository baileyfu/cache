package xcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

import xcache.em.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XCache {
	String key() default StringUtils.EMPTY;

	String[] remove() default StringUtils.EMPTY;

	int expiring() default 0;

	TimeUnit timeUnit() default TimeUnit.NULL;

	String prefix() default StringUtils.EMPTY;

	String suffix() default StringUtils.EMPTY;
}
