package com.black.condition.def;

import com.black.condition.annotation.ConditionalOnExpression;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;

public class DefaultConditionalOnExpression extends AbstractConditionalResolver{

    public DefaultConditionalOnExpression(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean support(AnnotatedElement element) {
        return element.isAnnotationPresent(ConditionalOnExpression.class);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        return true;
    }
}
