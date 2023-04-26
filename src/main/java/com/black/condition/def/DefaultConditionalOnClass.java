package com.black.condition.def;

import com.black.condition.annotation.ConditionalOnClass;
import com.black.condition.inter.ConditionalResolver;

import java.lang.reflect.AnnotatedElement;

public class DefaultConditionalOnClass implements ConditionalResolver {
    @Override
    public boolean support(AnnotatedElement element) {
        return element.isAnnotationPresent(ConditionalOnClass.class);
    }

    @Override
    public boolean parse(AnnotatedElement element, Object source) {
        //默认情况下没有处理逻辑
        return true;
    }
}
