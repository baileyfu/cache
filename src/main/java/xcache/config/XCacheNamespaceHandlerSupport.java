package xcache.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 注册命名空间对应的BeanDefinitionParser
 * 
 * @author bailey.fu
 * @date 2018年4月25日
 * @version 1.0
 * @description
 */
public class XCacheNamespaceHandlerSupport extends NamespaceHandlerSupport {
	@Override
	public void init() {
		registerBeanDefinitionParser("config", new ConfigNamespaceHandlerSupport());
		registerBeanDefinitionParser("autoproxy", new AutoproxyBeanDefinitionParser());
	}
}
