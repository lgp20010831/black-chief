package com.black.core.factory.beans.config;

import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.factory.beans.BeansUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.yml.YmlConfigurationProperties;

import java.util.Map;

public class YmlPropertiesMethodHandler implements BeanMethodHandler {


    @Override
    public boolean supportReturnProcessor(MethodWrapper method, Object returnValue) {
        return returnValue != null && !method.getReturnType().equals(void.class) &&
                (method.hasAnnotation(YmlConfigurationProperties.class));
    }

    @Override
    public Object processor(MethodWrapper method, Object returnValue, Object bean, BeanFactory factory) {
        YmlConfigurationProperties annotation = method.getAnnotation(YmlConfigurationProperties.class);
        String prefix = annotation.value();
        if (returnValue != null){
            ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
            Map<String, String> global = reader.groupQueryForGlobal(prefix, true);
            BeansUtils.wriedPropertiesBean(returnValue, global, !annotation.wriedFiled());
        }
        return BeanMethodHandler.super.processor(method, returnValue, bean, factory);
    }
}
