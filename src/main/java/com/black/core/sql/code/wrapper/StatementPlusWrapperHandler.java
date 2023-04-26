package com.black.core.sql.code.wrapper;

import com.black.core.aop.servlet.ParameterWrapper;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.WriedQueryStatement;
import com.black.core.sql.code.aop.WrapperMethodNotQualifiedException;
import com.black.core.util.AnnotationUtils;

public interface StatementPlusWrapperHandler {

    default boolean supportCreateConfiguration(MethodWrapper wrapper){
        return true;
    }

    default WrapperConfiguration createConfiguration(MethodWrapper wrapper) throws WrapperMethodNotQualifiedException {
        ParameterWrapper parameter = wrapper.getSingleParameterByAnnotation(WriedQueryStatement.class);
        if (parameter == null){
            throw new WrapperMethodNotQualifiedException();
        }
        WrapperConfiguration configuration = new WrapperConfiguration();
        configuration.setMw(wrapper);
        WriedQueryStatement annotation = parameter.getAnnotation(WriedQueryStatement.class);
        AnnotationUtils.loadAttribute(annotation, configuration);
        configuration.init();
        return configuration;
    }

    boolean support(WrapperConfiguration configuration);

    Object handler(Object arg, WrapperConfiguration configuration);
}
