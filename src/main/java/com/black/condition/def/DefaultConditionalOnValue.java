package com.black.condition.def;

import com.black.condition.annotation.ConditionalOnValue;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;

public class DefaultConditionalOnValue extends AbstractConditionalResolver{

    public DefaultConditionalOnValue(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean support(AnnotatedElement element) {
        return element.isAnnotationPresent(ConditionalOnValue.class);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        return true;
    }
}
