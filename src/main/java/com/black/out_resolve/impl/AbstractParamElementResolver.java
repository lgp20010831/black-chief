package com.black.out_resolve.impl;

import com.black.io.out.JHexByteArrayOutputStream;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

public abstract class AbstractParamElementResolver extends AbstractAnnotationElementResolver{


    @Override
    protected boolean supportElement(AnnotatedElement element) {
        return element instanceof Parameter;
    }

    @Override
    protected void doResolveElement(JHexByteArrayOutputStream outputStream, AnnotatedElement element, Object value) throws Throwable {
        Parameter parameter = (Parameter) element;
        resolveParam(outputStream, parameter, value);
    }

    protected abstract void resolveParam(JHexByteArrayOutputStream outputStream, Parameter parameter, Object value) throws Throwable;
}
