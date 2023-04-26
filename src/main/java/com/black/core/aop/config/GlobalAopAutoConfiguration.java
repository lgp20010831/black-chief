package com.black.core.aop.config;

import com.black.core.aop.code.AopChainProperties;
import com.black.core.aop.code.DefaultAopTaskAdapter;
import com.black.core.config.AbstractConfiguration;
import com.black.core.config.SpringAutoConfiguration;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Log4j2
@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@AutoConfigureBefore(SpringAutoConfiguration.class)
@Conditional(AopConfigurationConditional.class)
@EnableConfigurationProperties(AopChainProperties.class)
public class GlobalAopAutoConfiguration extends AbstractConfiguration implements BeanDefinitionRegistryPostProcessor {

    public GlobalAopAutoConfiguration(){
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        if (log.isInfoEnabled()) {
            log.info("create aop dispatcher ...");
        }
        DefaultAopTaskAdapter taskAdapter = new DefaultAopTaskAdapter();
        try {
            listableBeanFactory.registerSingleton(NameUtil.getName(taskAdapter), taskAdapter);
        }catch (BeansException e){
            if (log.isErrorEnabled()) {
                log.error("无法注册aop调度中心");
            }
            throw e;
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }


}
