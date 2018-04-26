package xcache.key;

import commons.beanutils.BeanUtils;

public class DumpFieldTransformer implements CacheKeyTransformer {
	@Override
	public Integer make(Object original) {
		String dumpInfo = BeanUtils.dump(original);
		return dumpInfo == null ? 0 : Math.abs(dumpInfo.hashCode());
	}

}
