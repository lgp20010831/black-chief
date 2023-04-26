package com.black.out_resolve.impl;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.core.query.Wrapper;

import java.lang.reflect.AnnotatedElement;

public abstract class AbstractAnnotationElementResolver extends AbstractJHexOutputStreamResolver{

    @Override
    protected boolean pareSupport(Object rack) {
        Object target = rack;
        if (rack instanceof Wrapper){
            target = ((Wrapper) rack).get();
        }
        return target instanceof AnnotatedElement;
    }

    @Override
    protected boolean accurateSupport(Object rack) {
        return supportElement(getElement(rack));
    }

    protected abstract boolean supportElement(AnnotatedElement element);

    protected AnnotatedElement getElement(Object rack){
        Object target = rack;
        if (rack instanceof Wrapper){
            target = ((Wrapper) rack).get();
        }
        return (AnnotatedElement) target;
    }


    protected abstract void doResolveElement(JHexByteArrayOutputStream outputStream, AnnotatedElement element, Object value) throws Throwable;

    @Override
    protected void resolveJHex(JHexByteArrayOutputStream outputStream, Object rack, Object value) throws Throwable {
        AnnotatedElement element = getElement(rack);
        doResolveElement(outputStream, element, value);
    }
}
