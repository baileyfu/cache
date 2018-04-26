package xcache.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import xcache.core.CacheConfiguration;

/**
 * 处理<config>标签
 * 
 * @author bailey.fu
 * @date 2018年4月25日
 * @version 1.0
 * @description
 */
public class ConfigNamespaceHandlerSupport implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        String localBeanId = element.getAttribute("local");
        String remoteBeanId = element.getAttribute("remote");

        Object localBean=org.apache.commons.lang3.StringUtils.isBlank(localBeanId)?null:new RuntimeBeanReference(localBeanId);
        Object remoteBean=org.apache.commons.lang3.StringUtils.isBlank(remoteBeanId)?null:new RuntimeBeanReference(remoteBeanId);
        
        RootBeanDefinition config = new RootBeanDefinition();
        config.setBeanClass(CacheConfiguration.class);
        ConstructorArgumentValues caValues=new ConstructorArgumentValues();
        caValues.addIndexedArgumentValue(0, localBean);
        caValues.addIndexedArgumentValue(1, remoteBean);
        config.setConstructorArgumentValues(caValues);

        BeanDefinitionHolder holder = new BeanDefinitionHolder(config, "CacheConfiguration");
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
        return config;
    }
}
