package com.black.core.factory.beans.lazy;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.CommonProxyHandler;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.Wrapper;

/**
 * @author 李桂鹏
 * @create 2023-05-23 11:18
 */
@SuppressWarnings("all")
public class LazyFactory {

    public static <T> T lazyProxyBean(BeanDefinitional<T> definitional, BeanFactory beanFactory){
        return (T) lazyProxyBean(definitional.getPrimordialClass(), beanFactory);
    }

    public static <T> T lazyProxyBean(Class<T> primordialClass, BeanFactory beanFactory){
        CommonProxyHandler proxyHandler = new CommonProxyHandler(new LazyBeanWrapper(primordialClass, beanFactory),
                new LazyBeanProxyLayer());
        //create lazy proxy
        Object cglib = ApplyProxyFactory.proxyCGLIB(primordialClass, proxyHandler, true, LazyBean.class);
        return (T) cglib;
    }


    static class LazyBeanWrapper implements Wrapper<Object> {

        private final Class<?> primordialClass;

        private final BeanFactory beanFactory;

        private Object instance;

        LazyBeanWrapper(Class<?> primordialClass, BeanFactory beanFactory) {
            this.primordialClass = primordialClass;
            this.beanFactory = beanFactory;
        }

        public synchronized Object getObject(){
            if (instance == null){
                BeanDefinitional<?> definitional = beanFactory.getDefinitional(primordialClass);
                Object singleBean = beanFactory.getSingleBean(primordialClass);
                if (singleBean instanceof LazyBean){
                    instance = beanFactory.doCreateBean(definitional, null);
                    beanFactory.replaceInstance((Class<Object>)primordialClass, instance);
                }else {
                    instance = singleBean;
                }
            }
            return instance;
        }

        @Override
        public Object get() {
            return getObject();
        }
    }
}
