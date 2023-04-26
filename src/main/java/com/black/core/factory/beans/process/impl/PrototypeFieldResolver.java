package com.black.core.factory.beans.process.impl;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.PrototypeBean;
import com.black.core.factory.beans.process.inter.BeanInitializationHandler;
import com.black.core.query.FieldWrapper;

public class PrototypeFieldResolver implements BeanInitializationHandler {
    @Override
    public boolean support(FieldWrapper fw, BeanFactory factory, Object bean) {
        return fw.hasAnnotation(PrototypeBean.class) && fw.isNull(bean);
    }

    @Override
    public void doHandler(FieldWrapper fw, BeanFactory factory, Object bean) {
        Class<?> fwType = fw.getType();
        PrototypeBean annotation = fw.getAnnotation(PrototypeBean.class);
        Object fwValue = PrototypeBeanResolverManager.resolvePrototypeType(fwType, annotation.createBatch(), factory, fw);
        fw.setValue(bean, fwValue);
    }


}
