package xcache.aspect;

@FunctionalInterface
public interface EnhancingFactory<P extends CacheEnhancer> {
	public P create(Object original);
}
