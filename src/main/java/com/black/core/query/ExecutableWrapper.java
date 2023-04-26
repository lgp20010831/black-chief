package com.black.core.query;

import java.lang.annotation.Annotation;

public interface ExecutableWrapper {


    boolean hasAnnotation(Class<? extends Annotation> type);

    <T extends Annotation> T getAnnotation(Class<T> type);
}
