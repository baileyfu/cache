package xcache.aspect;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	/** SpEL的分隔符 */
	private static final String SPEL_SIGN = "#";
	private Map<String, AnnoBean> gmMap = new HashMap<>();
	private Map<String, Collection<AnnoBean>> rmMap = new HashMap<>();
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
				/** 一个remove方法可匹配多个缓存方法 */
				Collection<AnnoBean> abColl = rmMap.get(methodName);
				if (abColl == null) {
					abColl = new HashSet<>();
					/** 以方法的名称为key，匹配所有重载方法 */
					rmMap.put(methodName, abColl);
				}
				abColl.add(ab);
			}
		}
	}

	protected AnnoBean annoInfo4get(String methodGenericString) {
		return gmMap.get(methodGenericString);
	}

	protected Collection<AnnoBean> annoInfo4remove(String methodName) {
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
		if (params != null && params.length > 0) {
			Object key = null;
			if (StringUtils.isBlank(annoBean.key)) {/** 未指定key则以第一个参数作为key */
				key = BeanUtils.dump(params[0]);
			} else if (annoBean.key.startsWith(SPEL_SIGN)) {/** SpEL */
				for (int i = 0; i < paramName.length; i++) {
					// 以第一个匹配到的参数为key,若无匹配项则返回null
					if ((SPEL_SIGN + paramName[i]).equals(annoBean.key) || paramName[i].equals(StringUtils.substringBetween(annoBean.key, SPEL_SIGN, "."))) {
						ExpressionParser parser = new SpelExpressionParser();
						StandardEvaluationContext context = new StandardEvaluationContext();
						context.setVariable(paramName[i], params[i]);
						key = parser.parseExpression(annoBean.key).getValue(context, Object.class);
						break;
					}
				}
				if (key == null) {
					return null;
				}
			} else {/** 指定字符串 */
				key = annoBean.key;
			}
			return new StringBuilder().append(annoBean.prefix).append(key).append(annoBean.suffix).toString();
		}
		return null;
	}
}
