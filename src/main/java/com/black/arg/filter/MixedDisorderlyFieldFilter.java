package com.black.arg.filter;

import com.black.core.log.IoLog;
import com.black.core.query.ClassWrapper;
import org.springframework.beans.factory.BeanFactory;

public class MixedDisorderlyFieldFilter implements DepthAnalysisFieldFilter{
    @Override
    public boolean canAnalysis(String name, Class<?> type, Object bean) {

        return !(type.equals(Class.class) ||
                type.isEnum() || type.isArray() ||
                type.isAnnotation() || ClassWrapper.isBasic(type.getName()) ||
                BeanFactory.class.isAssignableFrom(type) || IoLog.class.isAssignableFrom(type) ||
                type.getName().startsWith("org.springframework.cglib"));
    }
}
