package com.black.core.sql.code.aop;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.wrapper.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrapperParser {

    private final Map<Method, WrapperConfiguration> configurationMap = new ConcurrentHashMap<>();

    private final WrapperHandlerCollect handlerCollect = WrapperHandlerCollect.getInstance();

    public Object parseWrapper(MethodWrapper wrapper, Object arg){
        Method method = wrapper.getMethod();
        if (!configurationMap.containsKey(method)){
            try {
                parseConfig(wrapper);
            } catch (WrapperMethodNotQualifiedException e) {
                return null;
            }
        }

        WrapperConfiguration configuration = configurationMap.get(method);
        if (configuration != null){
            for (StatementPlusWrapperHandler handler : handlerCollect.getStatementHandlers()) {
                if (handler.support(configuration)) {
                    return handler.handler(arg, configuration);
                }
            }
        }
        return null;
    }

    protected void parseConfig(MethodWrapper wrapper) throws WrapperMethodNotQualifiedException{

        Method method = wrapper.getMethod();
        WrapperConfiguration configuration = null;
        try {
            for (StatementPlusWrapperHandler handler : handlerCollect.getStatementHandlers()) {
                if (handler.supportCreateConfiguration(wrapper)) {
                    configuration = handler.createConfiguration(wrapper);
                    break;
                }
            }
        }finally {
            if (configuration != null){
                configurationMap.put(method, configuration);
            }
        }
    }
}
