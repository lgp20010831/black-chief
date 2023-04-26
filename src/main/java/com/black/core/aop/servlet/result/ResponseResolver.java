package com.black.core.aop.servlet.result;

import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.factory.beans.component.BeanFactoryHolder;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@GlobalAround
public class ResponseResolver implements GlobalAroundResolver {

    private final Map<Method, Collection<ResponseBodyHandler>> handlerCache = new ConcurrentHashMap<>();

    private final InstanceFactory instanceFactory;

    public ResponseResolver(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    @Override
    public Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass) {
        Method method = httpMethodWrapper.getHttpMethod();
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        AdditionalResponseBody responseBody = wrapper.getAnnotation(AdditionalResponseBody.class);
        if (responseBody != null){
            try {

                if (!handlerCache.containsKey(method)) {
                    Set<ResponseBodyHandler> handlers = new HashSet<>();
                    boolean byBeanFactory = responseBody.instanceByBeanFactory();
                    Class<? extends ResponseBodyHandler>[] handlerClasses = responseBody.value();
                    for (Class<? extends ResponseBodyHandler> handlerClass : handlerClasses) {
                        if (byBeanFactory){
                            handlers.add( BeanFactoryHolder.getFactory().getSingleBean(handlerClass));
                        }else {
                            handlers.add(instanceFactory.getInstance(handlerClass));
                        }
                    }
                    handlerCache.put(method, handlers);
                }

                Collection<ResponseBodyHandler> responseBodyHandlers = handlerCache.get(method);
                if (result instanceof Collection){
                    for (ResponseBodyHandler handler : responseBodyHandlers) {
                        result = handler.processorCollection(httpMethodWrapper, (Collection<?>) result);
                    }
                }else if (result instanceof Map){
                    for (ResponseBodyHandler handler : responseBodyHandlers) {
                        result = handler.processorMap(httpMethodWrapper, (Map<?, ?>) result);
                    }
                }else {
                    for (ResponseBodyHandler handler : responseBodyHandlers) {
                        result = handler.processorUnknown(httpMethodWrapper, result);
                    }
                }
            }catch (RuntimeException e){
                if (log.isWarnEnabled()) {
                    log.warn("解析方法: {} 返回值时, 发生异常", method.getName());
                }
                CentralizedExceptionHandling.handlerException(e);
            }
        }
        return result;
    }


}
