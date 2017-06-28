package xcache.aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import commons.beanutils.BeanUtils;
import xcache.annotation.LCache;
import xcache.annotation.RCache;

/**
 * 原类型分析以及key生成
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-22 14:27
 */
public abstract class EnhancingResolver implements CacheEnhancer {
	private static final String KEY_SIGN = "_";
	private Map<String, AnnoBean> gmMap = new HashMap<>();
	private Map<String, List<AnnoBean>> rmMap = new HashMap<>();
	private Class<?> clazz;

	public EnhancingResolver(Object target) {
		clazz = target.getClass();
		for (Method m : clazz.getDeclaredMethods()) {
			RCache rCache = m.getDeclaredAnnotation(RCache.class);
			if (rCache != null) {
				AnnoBean ab = AnnoBean.toAnnoBean(rCache);
				/** 以方法的详细描述为key，匹配唯一方法 */
				gmMap.put(m.toGenericString(), ab);
				saveRemoveMethods(ab);
				// 一个方法只解析一种缓存；RCache优先
				continue;
			}
			LCache lCache = m.getDeclaredAnnotation(LCache.class);
			if (lCache != null) {
				AnnoBean ab = AnnoBean.toAnnoBean(lCache);
				gmMap.put(m.toGenericString(), ab);
				saveRemoveMethods(ab);
			}
		}
	}

	private void saveRemoveMethods(AnnoBean ab) {
		if (ab.remove != null && ab.remove.length > 0) {
			for (String methodName : ab.remove) {
				if(StringUtils.isNotBlank(methodName)){
					/** 一个remove方法可匹配多个缓存方法 */
					List<AnnoBean> abColl = rmMap.get(methodName);
					if (abColl == null) {
						abColl = new ArrayList<>();
					}
					abColl.add(ab);
					/** 以方法的名称为key，匹配所有重载方法 */
					rmMap.put(methodName, abColl);
				}
			}
		}
	}

	protected AnnoBean annoInfo4get(String methodGenericString) {
		return gmMap.get(methodGenericString);
	}

	protected List<AnnoBean> annoInfo4remove(String methodName) {
		return rmMap.get(methodName);
	}

	protected boolean isGetCache(String methodGenericString) {
		return gmMap.containsKey(methodGenericString);
	}

	protected boolean isRemoveCache(String methodName) {
		return rmMap.containsKey(methodName);
	}

	/**
	 * key生成规则；三种情况：<br/>
	 * 1.未指定key，则以方法的第一个参数dump成字符串作为key <br/>
	 * 2.SpEL，表达式引用参数名称正确则解析，否则将表达式以字符串形式作为key；若多个参数名称相同，则取第一个参数参与表达式运算<br/>
	 * 3.字符串形式，直接作为key
	 * 
	 * @param annoBean
	 * @param method
	 * @param params
	 * @return
	 */
	protected Object renderKey(AnnoBean annoBean, String[] paramName, Object[] params) {
		Object key = null;
		if (StringUtils.isBlank(annoBean.key)) {/** 未指定key则以第一个参数作为key */
			key = ArrayUtils.isEmpty(params) ? null : BeanUtils.dump(params[0]);
		} else {/** 不能解析为SpEL的则以字符串形式作为key */
			if (!ArrayUtils.isEmpty(params)) {
				boolean hasMatched = false;
				Collection<String> existed = new ArrayList<>();
				ExpressionParser parser = new SpelExpressionParser();
				StandardEvaluationContext context = new StandardEvaluationContext();
				for (int i = 0; i < paramName.length; i++) {
					// 以第一个匹配到的参数为key,若无匹配项则返回null
					if (!existed.contains(paramName[i])) {
						existed.add(paramName[i]);
						if (annoBean.key.contains(paramName[i])) {
							context.setVariable(paramName[i], params[i]);
							hasMatched = true;
						}
					}
				}
				try {
					key = hasMatched ? parser.parseExpression(annoBean.key).getValue(context, Object.class) : null;
				} catch (Exception e) {}
			}
			key = key == null ? annoBean.key : key;
		}
		return key == null
				? null
				: new StringBuilder()
						.append(annoBean.prefix)
						.append(StringUtils.isNotBlank(annoBean.prefix) ? KEY_SIGN : StringUtils.EMPTY)
						.append(key)
						.append(StringUtils.isNotBlank(annoBean.suffix) ? KEY_SIGN : StringUtils.EMPTY)
						.append(annoBean.suffix)
						.toString();
	}
}
