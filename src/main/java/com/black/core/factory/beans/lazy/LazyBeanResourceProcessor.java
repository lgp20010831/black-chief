package com.black.core.factory.beans.lazy;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.annotation.AsLazy;
import com.black.core.factory.beans.process.inter.BeanInitializationHandler;
import com.black.core.factory.beans.process.inter.BeanPostProcessor;
import com.black.core.query.FieldWrapper;

import java.util.Collection;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-23 13:38
 */
@SuppressWarnings("all")
public class LazyBeanResourceProcessor implements BeanInitializationHandler {

    @Override
    public boolean support(FieldWrapper fw, BeanFactory factory, Object bean) {
        return fw.hasAnnotation(AsLazy.class);
    }

    @Override
    public void doHandler(FieldWrapper fw, BeanFactory factory, Object bean) {
        Class<?> type = fw.getType();
        if (Collection.class.isAssignableFrom(type)){
            throw new BeanFactorysException("Lazy loading cannot be defined into " +
                    "collection properties");
        }

        if (Map.class.isAssignableFrom(type)){
            throw new BeanFactorysException("Lazy loading cannot be defined into " +
                    "map properties");
        }

        Object proxyBean = LazyFactory.lazyProxyBean(type, factory);
        fw.setValue(bean, proxyBean);
    }
}
