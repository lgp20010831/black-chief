package com.black.core.factory.beans.config;

import com.black.condition.annotation.ConditionalOnClass;
import com.black.condition.def.AbstractConditionalResolver;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;

public class BeanFactoryConditionalOnClass extends AbstractConditionalResolver {

    public BeanFactoryConditionalOnClass(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean support(AnnotatedElement element) {
        return element.isAnnotationPresent(ConditionalOnClass.class);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        ConditionalOnClass annotation = element.getAnnotation(ConditionalOnClass.class);
        Class<?>[] valueClasses = annotation.value();
        for (Class<?> valueClass : valueClasses) {
            if (!factory.containBean(valueClass)) {
                return false;
            }
        }
        return true;
    }
}
