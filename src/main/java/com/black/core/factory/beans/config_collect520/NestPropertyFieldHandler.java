package com.black.core.factory.beans.config_collect520;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanInitializationHandler;
import com.black.core.query.FieldWrapper;

@SuppressWarnings("all")
public class NestPropertyFieldHandler extends AbstractNestPropertyHandler implements BeanInitializationHandler {
    @Override
    public boolean support(FieldWrapper fw, BeanFactory factory, Object bean) {
        return fw.hasAnnotation(NestProperty.class);
    }

    @Override
    public void doHandler(FieldWrapper fw, BeanFactory factory, Object bean) {
        Object value = fw.getValue(bean);
        if (value == null){
            value = factory.getSingleBean(fw.getType());
            fw.setValue(bean, value);
        }
        handlerTarget(value, factory);
    }

}
