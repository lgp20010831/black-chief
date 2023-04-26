package com.black.core.ill.aop;

import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class IllIntercept implements AopTaskIntercepet {

    private final IllAopHybrid hybrid;
    private final ThreadLocal<Map<Method, IllSourceWrapper>> sourceLocal = new ThreadLocal<>();


    public IllIntercept(IllAopHybrid hybrid) {
        this.hybrid = hybrid;
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        final Method method = hijack.getMethod();
        final Object[] args = hijack.getArgs();
        Object result;
        try {

            result = hijack.doRelease(args);
        }catch (Throwable ex){
            ThrowableCaptureConfiguration configuration = hybrid.getThrowableConfigurationMutes().get(method);
            if (configuration == null){
                if (log.isWarnEnabled()) {
                    log.warn("The agent exception method can't find the" +
                            " configuration information of this method, " +
                            "so it can't handle this method exception globally");
                }
                throw ex;
            }
            Map<Method, IllSourceWrapper> wrapperMap = sourceLocal.get();
            if(wrapperMap == null){
                wrapperMap = new ConcurrentHashMap<>();
                sourceLocal.set(wrapperMap);
            }

            IllSourceWrapper wrapper = wrapperMap.computeIfAbsent(method, met -> {
                Object bean = hijack.getInvocation().getThis();
                return new IllSourceWrapper(bean, met, configuration);
            });

            wrapper.setError(ex);
            wrapper.setArgs(args);
            try {
                result = GlobalThrowableHandlerManagement.handlerThrowable(wrapper);
            }finally {
                GlobalThrowableHandlerManagement.close();
            }
        }
        return result;
    }
}
