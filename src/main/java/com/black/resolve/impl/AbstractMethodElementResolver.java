package com.black.resolve.impl;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.query.MethodWrapper;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

@SuppressWarnings("all")
public abstract class AbstractMethodElementResolver extends AbstractAnnotationElementResolver{

    @Override
    protected boolean pareSupport(Object rack) {
        boolean isae = super.pareSupport(rack);
        if (isae){
            AnnotatedElement element = getAnnotationElement(rack);
            return element instanceof Method;
        }
        return false;
    }

    @Override
    protected boolean concreteSupport(Object rack) {
        return concreteMethodSupport(MethodWrapper.get((Method) getAnnotationElement(rack)));
    }

    protected boolean concreteMethodSupport(MethodWrapper mw){
        return false;
    }

    @Override
    protected Object resolveAnnotationElement(AnnotatedElement annotatedElement, JHexByteArrayInputStream inputStream) throws Throwable {
        Method method = (Method) annotatedElement;
        MethodWrapper mw = MethodWrapper.get(method);
        return resolveMethod(mw, inputStream);
    }

    protected abstract Object resolveMethod(MethodWrapper mw, JHexByteArrayInputStream inputStream) throws Throwable;

}
