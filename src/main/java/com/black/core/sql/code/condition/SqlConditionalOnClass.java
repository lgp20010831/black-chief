package com.black.core.sql.code.condition;

import com.black.condition.annotation.ConditionalOnClass;
import com.black.condition.def.AbstractConditionalResolver;
import com.black.core.factory.beans.BeanFactory;

import java.lang.reflect.AnnotatedElement;

public class SqlConditionalOnClass extends AbstractConditionalResolver {

    public SqlConditionalOnClass(BeanFactory factory) {
        super(factory);
    }

    @Override
    public boolean support(AnnotatedElement element) {
        return element.isAnnotationPresent(ConditionalOnClass.class);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        throw new IllegalStateException("sql is not support condition of class");
    }
}
