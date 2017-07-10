package xcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

import xcache.em.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RCache {
	/**
	 * 缓存的key；可为空，或字符串，或SpEL表达式
	 * 
	 * @return
	 */
	String key() default StringUtils.EMPTY;

	/***
	 * 触发从缓存删除的方法<br/>
	 * 1.目前只支持同一类中的方法，且remove方法的参数跟key修饰的方法参数一致；<br/>
	 * 2.remove方法最好不要重复标注在其他方法上，否则容易出错
	 * 3.若一个方法既是缓存方法又是remove方法，则忽略remove方法
	 * 
	 * @return
	 */
	String[] remove() default StringUtils.EMPTY;

	/**
	 * 缓存操作是否抛出异常
	 * 
	 * @return
	 */
	boolean throwable() default false;

	/**
	 * 有效期
	 * 
	 * @return
	 */
	int expiring() default 0;

	TimeUnit timeUnit() default TimeUnit.NULL;

	String prefix() default StringUtils.EMPTY;

	String suffix() default StringUtils.EMPTY;
}
