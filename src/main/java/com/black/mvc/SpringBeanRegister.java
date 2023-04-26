package com.black.mvc;

import com.black.holder.SpringHodler;
import com.black.spring.BeanEnhanceWrapper;
import com.black.user.Identity;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class SpringBeanRegister {


    public static void registerBean(Object bean, boolean proxy){
        Object single = bean;
        if (bean instanceof BeanEnhanceWrapper){
            single = ((BeanEnhanceWrapper) bean).getBean();
        }
        String name;
        if (single instanceof Identity){
            name = ((Identity) single).getName();
        }else {
            Class<?> primordialClass = single.getClass();
            name = primordialClass.getSimpleName();
        }
        registerBean(name, bean, proxy);
    }

    public static void registerBean(String name, Object bean, boolean proxy){
        DefaultListableBeanFactory beanFactory = SpringHodler.getNonNullListableBeanFactory();
        beanFactory.autowireBean(bean);
        if (proxy){
            bean = AdvisorWeavingFactory.proxy(bean, true);
        }
        beanFactory.registerSingleton(name, bean);
    }


}
