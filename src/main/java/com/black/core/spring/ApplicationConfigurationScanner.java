package com.black.core.spring;

import com.black.holder.SpringHodler;
import com.black.core.json.ReflexUtils;
import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.ReusingProxyFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

@Log4j2
public class ApplicationConfigurationScanner implements PostPatternClazzDriver {
    @Override
    public void postPatternClazz(Class<?> beanClazz,
                                 Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                 ReusingProxyFactory proxyFactory,
                                 ChiefExpansivelyApplication chiefExpansivelyApplication) {

        if (beanClazz.isInterface() || Modifier.isAbstract(beanClazz.getModifiers()) || beanClazz.isEnum())
            return;
        if (AnnotationUtils.getAnnotation(beanClazz, Configuration.class) != null){

            Collection<Object> applicationConfigurationMutes = chiefExpansivelyApplication.getApplicationConfigurationMutes();
            BeanFactory beanFactory = SpringHodler.getListableBeanFactory();
            try {
                Object bean = beanFactory == null ? ReflexUtils.instance(beanClazz) : beanFactory.getBean(beanClazz);
                applicationConfigurationMutes.add(bean);
            }catch (BeansException e){
                if (log.isDebugEnabled()) {
                    log.debug("获取配置类失败, {}", beanClazz);
                }
            }
        }
    }
}
