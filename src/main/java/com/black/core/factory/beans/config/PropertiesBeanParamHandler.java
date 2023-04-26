package com.black.core.factory.beans.config;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.factory.beans.Properties;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class PropertiesBeanParamHandler implements BeanMethodHandler {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return parameter.hasAnnotation(Properties.class);
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        Properties properties;
        String prefix = (properties = parameter.getAnnotation(Properties.class)).value();
        Map<String, String> global = ApplicationConfigurationReaderHolder.getReader().groupQueryForGlobal(prefix, true);
        if (previousValue != null){
            PropertiesWiredManager.wiredObject(global, previousValue, !properties.wriedFiled());
        }else {
            if (log.isWarnEnabled()) {
                log.warn("Property is injected into the object, but the object is not instantiated. " +
                        "Please mark other annotations to make other processors sensitive and instantiate " +
                        "in advance, hit param: [{}]", parameter.getName());
            }
            previousValue = PropertiesWiredManager.wiredNullObject(global, parameter.getType(), !properties.wriedFiled(), factory);
        }
        return previousValue;
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        Properties properties;
        String prefix = (properties = pw.getAnnotation(Properties.class)).value();
        Map<String, String> global = ApplicationConfigurationReaderHolder.getReader().groupQueryForGlobal(prefix, true);
        if (previousValue != null){
            PropertiesWiredManager.wiredObject(global, previousValue, !properties.wriedFiled());
        }else {
            if (log.isWarnEnabled()) {
                log.warn("Property is injected into the object, but the object is not instantiated. " +
                        "Please mark other annotations to make other processors sensitive and instantiate " +
                        "in advance, hit param: [{}]", pw.getName());
            }
            previousValue = PropertiesWiredManager.wiredNullObject(global, pw.getType(), !properties.wriedFiled(), factory);
        }
        return previousValue;
    }
}
