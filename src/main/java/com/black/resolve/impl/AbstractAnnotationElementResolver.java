package com.black.resolve.impl;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.query.Wrapper;

import java.lang.reflect.AnnotatedElement;

public abstract class AbstractAnnotationElementResolver extends AbstractResolver{

    protected AnnotatedElement getAnnotationElement(Object rack){
        AnnotatedElement element;
        if (rack instanceof Wrapper){
            element = (AnnotatedElement) ((Wrapper<?>) rack).get();
        }else {
            element = (AnnotatedElement) rack;
        }
        return element;
    }

    @Override
    protected boolean pareSupport(Object rack) {
        Object target = rack;
        if (rack instanceof Wrapper){
            target = ((Wrapper<?>) rack).get();
        }
        return target instanceof AnnotatedElement;
    }

    @Override
    public Object doResolve(Object rack, JHexByteArrayInputStream inputStream) throws Throwable {
        return resolveAnnotationElement(getAnnotationElement(rack), inputStream);
    }

    protected abstract Object resolveAnnotationElement(AnnotatedElement annotatedElement, JHexByteArrayInputStream inputStream) throws Throwable;

}
