package com.black.condition.inter;

import java.lang.reflect.AnnotatedElement;

public interface ConditionalResolver {

    boolean support(AnnotatedElement element);

    boolean parse(AnnotatedElement element, Object source);
}
