package com.black.core.factory.beans;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.json.Value;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class ApplicationConfigurationMethodHandler implements BeanMethodHandler {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return ew instanceof ConstructorWrapper || parameter.hasAnnotation(Value.class);
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        if (previousValue != null){
            return previousValue;
        }
        Value valueAnnotation = parameter.getAnnotation(Value.class);
        String txt = valueAnnotation.value();
        if (!StringUtils.hasText(txt)){
            txt = parameter.getName();
        }
        return doResolver(txt, parameter);
    }


    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        if (previousValue != null){
            return previousValue;
        }
        String txt;
        if (pw.hasAnnotation(Value.class)){
            txt = pw.getAnnotation(Value.class).value();
        }else {
            txt = pw.getName();
        }
        return doResolver(txt, pw);
    }


    private Object doResolver(String txt, ParameterWrapper pw){
        Object paramValue;
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        Map<String, String> configSource = reader.getMasterAndSubApplicationConfigSource();
        if (fromConfig(txt)){
            paramValue = Utils.parse(txt, "${", "}", configSource);
        }else{
            paramValue = configSource.get(txt);
        }
        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        if (typeHandler != null){
            paramValue = typeHandler.convert(pw.getType(), paramValue);
        }
        return paramValue;
    }


    private boolean fromConfig(String value){
        return value.contains("${");
    }
}
