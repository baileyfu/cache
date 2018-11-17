package com.lz.components.cache.core;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.lz.components.cache.annotation.LCache;
import com.lz.components.cache.annotation.RCache;
import com.lz.components.cache.aspect.CGLibEnhancer;
import com.lz.components.cache.aspect.CacheEnhancer;
import com.lz.components.cache.aspect.EnhancingFactory;
import com.lz.components.common.beanutil.BeanCopierUtils;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-20 14:01
 */
@Component
public class CacheBeanPostProcessor implements BeanPostProcessor {
	EnhancingFactory<CacheEnhancer> enhancingFactory = CGLibEnhancer::create;

	@Override
	public Object postProcessAfterInitialization(Object arg0, String arg1) throws BeansException {
		for (Method m : arg0.getClass().getDeclaredMethods()) {
			RCache rc = m.getDeclaredAnnotation(RCache.class);
			if (rc == null) {
				LCache lc = m.getDeclaredAnnotation(LCache.class);
				if (lc == null) {
					continue;
				}
			}
			try {
				Object enhancedBean=enhancingFactory.create(arg0).enhance();
				BeanCopierUtils.copyParentAttribute(arg0, enhancedBean);
				return enhancedBean;
			} catch (Exception e) {
				throw new FatalBeanException("create enhanced cache bean error :" + arg1, e);
			}
		}
		return arg0;
	}

	@Override
	public Object postProcessBeforeInitialization(Object arg0, String arg1) throws BeansException {
		return arg0;
	}
}
