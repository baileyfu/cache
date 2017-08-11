package xcache.aspect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import commons.fun.Supplier;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import xcache.CacheManager;

/**
 * 基于CGLib增强
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-06-20 13:59
 */
public class CGLibEnhancer extends EnhancingResolver {
	private Enhancer enhancer;

	public CGLibEnhancer(Object original) {
		super(original.getClass());
		ParameterNameDiscoverer pnd = new LocalVariableTableParameterNameDiscoverer();
		MethodInterceptor getMethod = new MethodInterceptor() {
			@Override
			public Object intercept(Object target, Method method, Object[] params, MethodProxy proxy) throws Throwable {
				return excuteGet(method, (annoBean) -> renderKey(annoBean, pnd.getParameterNames(method), params), () -> {
					try {
						return proxy.invoke(original, params);
					} catch (Throwable e) {
						throw new Exception(e);
					}
				});
			}
		};
		MethodInterceptor removeMethod = new MethodInterceptor() {
			@Override
			public Object intercept(Object target, Method method, Object[] params, MethodProxy proxy) throws Throwable {
				return excuteRemove(method, (annoBean) -> renderKey(annoBean, pnd.getParameterNames(method), params), () -> {
					try {
						return proxy.invoke(original, params);
					} catch (Throwable e) {
						throw new Exception(e);
					}
				});
			}
		};
		enhancer = new Enhancer();
		enhancer.setSuperclass(original.getClass());
		enhancer.setCallbacks(new Callback[] { getMethod, removeMethod, NoOp.INSTANCE });
		enhancer.setCallbackFilter(new CallbackFilter() {
			@Override
			public int accept(Method method) {
				return isGetCache(method.toGenericString()) ? 0 : isRemoveCache(method.getName()) ? 1 : 2;
			}
		});
	}

	private Object excuteGet(Method method, Function<AnnoBean, Object> renderKey, Supplier<Object> original) throws Exception {
		CacheManager cacheManager = CacheManager.getInstance();
		// CacheManager未初始化则不执行缓存逻辑
		if (cacheManager == null) {
			return original.get();
		}
		AnnoBean annoBean = annoInfo4get(method.toGenericString());
		Object key = renderKey.apply(annoBean);
		/** 若方法无参数，或无法匹配key则无法缓存 */
		if (key == null) {
			return original.get();
		}
		Object result = $doc(annoBean, () -> cacheManager.getRemote(annoBean.shardName, key), () -> cacheManager.getLocal(annoBean.shardName, key));
		if (result == null) {
			result = original.get();
			Object value = result;
			$doc(annoBean, () -> {
				if (annoBean.expiring < 1) {
					cacheManager.putToRemote(annoBean.shardName, key, value);
				} else {
					cacheManager.putToRemote(annoBean.shardName, key, value, annoBean.expiring, annoBean.timeUnit);
				}
				return null;
			}, () -> {
				if (annoBean.expiring < 1) {
					cacheManager.putToLocal(annoBean.shardName, key, value);
				} else {
					cacheManager.putToLocal(annoBean.shardName, key, value, annoBean.expiring, annoBean.timeUnit);
				}
				return null;
			});
		}
		return result;
	}

	private Object excuteRemove(Method method, Function<AnnoBean, Object> renderKey, Supplier<Object> original) throws Exception {
		Object result = original.get();
		CacheManager cacheManager = CacheManager.getInstance();
		// CacheManager未初始化则不执行缓存逻辑
		if (cacheManager != null) {
			List<AnnoBean> annoBeanList = annoInfo4remove(method.getName());
			for (AnnoBean annoBean : annoBeanList) {
				Object key = renderKey.apply(annoBean);
				/** 若方法无参数，则无需清除缓存 */
				if (key == null) {
					continue;
				}
				$doc(annoBean, () -> {
					cacheManager.removeRemote(annoBean.shardName, key);
					return null;
				}, () -> {
					cacheManager.removeLocal(annoBean.shardName, key);
					return null;
				});
			}
		}
		return result;
	}

	private Object $doc(AnnoBean annoBean, Supplier<Object> doRemote, Supplier<Object> doLocal) throws Exception {
		try {
			if (annoBean.isRemote()) {
				return doRemote.get();
			} else if (annoBean.isLocal()) {
				return doLocal.get();
			}
		} catch (Exception e) {
			if (annoBean.throwable) {
				throw e;
			}
		}
		return null;
	}

	@Override
	public Object enhance() throws Exception {
		return enhancer.create();
	}

	private static Map<String, CGLibEnhancer> instanceMap = new HashMap<>();

	@Deprecated
	public static CGLibEnhancer createInstance(Object original) {
		CGLibEnhancer instance = instanceMap.get(original.getClass().getTypeName());
		if (instance == null) {
			instance = new CGLibEnhancer(original);
			instanceMap.put(original.getClass().getTypeName(), instance);
		}
		return instance;
	}
	public static CGLibEnhancer create(Object original) {
		return new CGLibEnhancer(original);
	}
}
