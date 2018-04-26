package xcache.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import xcache.core.CacheBeanPostProcessor;

/**
 * 处理<autoproxy>标签
 * 
 * @author bailey.fu
 * @date 2018年4月25日
 * @version 1.0
 * @description
 */
public class AutoproxyBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		RootBeanDefinition cacheBeanPostProcessor = new RootBeanDefinition();
		cacheBeanPostProcessor.setBeanClass(CacheBeanPostProcessor.class);

		BeanDefinitionHolder holder = new BeanDefinitionHolder(cacheBeanPostProcessor, "CacheBeanPostProcessor");
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
		return cacheBeanPostProcessor;
	}
}
