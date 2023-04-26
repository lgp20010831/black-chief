package com.black.core.factory.beans.process.inter;


import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;

public interface BeanLifeCycleProcessor extends BeanPostProcessor {

    default boolean interceptBeanInstance(BeanDefinitional<?> definitional, BeanFactory beanFactory){
        return false;
    }

    default Object beforeBeanInstance(BeanDefinitional<?> definitional, BeanFactory beanFactory, Object chainPreviousBean) throws Throwable{
        return null;
    }

    /**
     *
     * @param dryBean
     * @param definitional
     * @param beanFactory
     * @return true if properties should be set on the bean;
     *          false if property population should be skipped. Normal
     *          implementations should return true. Returning false
     *          will also prevent any subsequent InstantiationAwareBeanPostProcessor
     *          instances being invoked on this bean instance.
     * @throws Throwable
     */
    default boolean afterBeanInstance(Object dryBean, BeanDefinitional<?> definitional, BeanFactory beanFactory) throws Throwable{
        return true;
    }

    default Object beforeBeanInitialize(Object dryBean, BeanDefinitional<?> definitional, BeanFactory factory) throws Throwable{
        return dryBean;
    }

    default Object afterBeanInitialize(Object completeBean, BeanDefinitional<?> definitional, BeanFactory factory) throws Throwable{
        return completeBean;
    }

    default void postBeanCancel(Object bean) throws Throwable{}
}
