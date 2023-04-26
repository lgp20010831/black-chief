package com.black.core.api.handler;

import com.black.core.api.ApiWrapperFactory;
import com.black.core.api.pojo.ApiParameterDetails;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public interface RequestParamHandler {


    Class<? extends Annotation> filterUselessParamsAnnoation(Class<?> pojoClass);

    boolean filterRemainingParam(Class<?> fieldType, Field field, String fieldName);

    void postWrapper(Class<?> controllerClass, Field field, ApiParameterDetails initDetails);

    void filterMethodParams(Class<?> controllerClass, Method method, List<ApiParameterDetails> details,
                            List<Class<?>> dependPojoClass, ApiWrapperFactory wrapperFactory);
}
