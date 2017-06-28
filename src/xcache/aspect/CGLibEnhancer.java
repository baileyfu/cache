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
	private MethodInterceptor getMethod;
	private MethodInterceptor removeMethod;
	private CallbackFilter callbackFilter;
	private Enhancer enhancer;

	public CGLibEnhancer(Object original) {
		super(original);
		ParameterNameDiscoverer pnd = new LocalVariableTableParameterNameDiscoverer();
		getMethod = new MethodInterceptor() {
			@Override
			public Object intercept(Object target, Method method, Object[] params, MethodProxy proxy) throws Throwable {
				return excuteGet(method, (annoBean) -> renderKey(annoBean, pnd.getParameterNames(method), params), () -> {
					try {
						return proxy.invokeSuper(target, params);
					} catch (Throwable e) {
						throw new Exception(e);
					}
				});
			}
		};
		removeMethod = new MethodInterceptor() {
			@Override
			public Object intercept(Object target, Method method, Object[] params, MethodProxy proxy) throws Throwable {
				return excuteRemove(method, (annoBean) -> renderKey(annoBean, pnd.getParameterNames(method), params), () -> {
					try {
						return proxy.invokeSuper(target, params);
					} catch (Throwable e) {
						throw new Exception(e);
					}
				});
			}
		};
		callbackFilter = new CallbackFilter() {
			@Override
			public int accept(Method method) {
				return isGetCache(method.toGenericString()) ? 0 : isRemoveCache(method.getName()) ? 1 : 2;
			}
		};
		enhancer = new Enhancer();
		enhancer.setSuperclass(original.getClass());
		enhancer.setCallbacks(new Callback[] { getMethod, removeMethod, NoOp.INSTANCE });
		enhancer.setCallbackFilter(callbackFilter);
	}

	private Object excuteGet(Method method, Function<AnnoBean, Object> renderKey, Supplier<Object> original) throws Exception {
		AnnoBean annoBean = annoInfo4get(method.toGenericString());
		Object key = renderKey.apply(annoBean);
		/** 若方法无参数，或无法匹配key则无法缓存 */
		if (key == null) {
			return original.get();
		}
		CacheManager cacheManager = CacheManager.getInstance();
		Object result = $doc(annoBean, () -> cacheManager.getRemote(key), () -> cacheManager.getLocal(key));
		if (result == null) {
			result = original.get();
			Object value = result;
			$doc(annoBean, () -> {
				if (annoBean.expiring < 1) {
					cacheManager.putToRemote(key, value);
				} else {
					cacheManager.putToRemote(key, value, annoBean.expiring, annoBean.timeUnit);
				}
				return null;
			}, () -> {
				if (annoBean.expiring < 1) {
					cacheManager.putToLocal(key, value);
				} else {
					cacheManager.putToLocal(key, value, annoBean.expiring, annoBean.timeUnit);
				}
				return null;
			});
		}
		return result;
	}

	private Object excuteRemove(Method method, Function<AnnoBean, Object> renderKey, Supplier<Object> original) throws Exception {
		Object result = original.get();
		List<AnnoBean> annoBeanList = annoInfo4remove(method.getName());
		for (AnnoBean annoBean:annoBeanList) {
			Object key=renderKey.apply(annoBean);
			/** 若方法无参数，则无需清除缓存 */
			if (key == null) {
				continue;
			}
			CacheManager cacheManager = CacheManager.getInstance();
			$doc(annoBean, () -> {
				cacheManager.removeRemote(key);
				return null;
			}, () -> {
				cacheManager.removeLocal(key);
				return null;
			});
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

	public static CGLibEnhancer create(Object original) {
		CGLibEnhancer instance = instanceMap.get(original.getClass().getTypeName());
		if (instance == null) {
			instance = new CGLibEnhancer(original);
			instanceMap.put(original.getClass().getTypeName(), instance);
		}
		return instance;
	}
}
