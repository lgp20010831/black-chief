package com.black.callback;

import com.black.spring.ChiefAgencyListableBeanFactory;
import com.black.spring.ChiefSpringHodler;
import com.black.core.annotation.Sort;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author shkstart
 * @create 2023-04-19 10:18
 */
@Sort(3000)
@Log4j2 @SuppressWarnings("all")
public class BeanFactoryEnhanceHook implements SpringApplicationRunListener {

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory instanceof ChiefAgencyListableBeanFactory){
            log.info("bean factory It has been enhanced");
        }else {
            try {
                DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
                Map<String, Object> singletonMutex = (Map<String, Object>) listableBeanFactory.getSingletonMutex();
                ChiefAgencyListableBeanFactory chiefAgencyListableBeanFactory = new ChiefAgencyListableBeanFactory();
                chiefAgencyListableBeanFactory.setApplicationStartup(listableBeanFactory.getApplicationStartup());
                singletonMutex.forEach((beanName, bean) -> {
                    chiefAgencyListableBeanFactory.registerSingleton(beanName, bean);
                });
                Field beanFactoryField = GenericApplicationContext.class.getDeclaredField("beanFactory");
                beanFactoryField.setAccessible(true);
                beanFactoryField.set(context, chiefAgencyListableBeanFactory);
                ChiefSpringHodler.setApplicationContext(context);
                ChiefSpringHodler.setChiefAgencyListableBeanFactory(chiefAgencyListableBeanFactory);
                log.info("enhanced bean factory is chiefAgencyListableBeanFactory");
            }catch (Throwable e){
                log.warn("无法增强 bean factory: " + ServiceUtils.getThrowableMessage(e));
            }
        }
    }
}
