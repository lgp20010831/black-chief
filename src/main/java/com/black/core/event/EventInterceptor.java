package com.black.core.event;

import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class EventInterceptor implements AopTaskIntercepet {

    public static boolean logError = false;
    private final Map<String, Collection<EventOperatorWrapper>> eventMap = new ConcurrentHashMap<>();

    private final Map<String, Collection<EventErrorWrapper>> errorMap = new ConcurrentHashMap<>();

    private ThreadPoolExecutor poolExecutor;

    public EventInterceptor(){
        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        WriteEvent writeEvent = AnnotationUtils.getAnnotation(method, WriteEvent.class);
        String[] entries = null;
        if (writeEvent != null){
            entries = writeEvent.value();
        }
        Object result = hijack.doRelease(hijack.getArgs());
        if (entries != null){
            for (String entry : entries) {
                Collection<EventOperatorWrapper> operatorWrappers = eventMap.get(entry);
                if(operatorWrappers != null){
                    for (EventOperatorWrapper operatorWrapper : operatorWrappers) {
                        if (operatorWrapper.isAsyn()){
                            poolExecutor.execute(() ->{
                                try {
                                    operatorWrapper.invoke(result, entry);
                                } catch (Throwable e) {
                                    handlerError(entry, e, result);
                                }
                            });
                        }else {
                            try {
                                operatorWrapper.invoke(result, entry);
                            }catch (Throwable e){
                                handlerError(entry, e, result);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    protected void handlerError(String entry, Throwable e, Object result){
        if (logError){
            CentralizedExceptionHandling.handlerException(e);
        }
        //do throw error
        Collection<EventErrorWrapper> errorWrappers = errorMap.get(entry);
        if (errorWrappers != null){
            for (EventErrorWrapper errorWrapper : errorWrappers) {
                if (errorWrapper.isAsyn()) {
                    poolExecutor.execute(() -> errorWrapper.invoke(e, result, entry));
                }else {
                    errorWrapper.invoke(e, result, entry);
                }
            }
        }
    }

    public void end(Set<String> entries){
        if (log.isInfoEnabled()) {
            log.info("collect entries:{}, monitored events:{}, with exception handling:{}",
                    entries, eventMap.keySet(), errorMap.keySet());
        }
    }

    public void setPoolExecutor(ThreadPoolExecutor poolExecutor) {
        this.poolExecutor = poolExecutor;
    }

    public void put(String key, EventOperatorWrapper wrapper){
        eventMap.computeIfAbsent(key, k -> new HashSet<>()).add(wrapper);
    }

    public void put(String key, EventErrorWrapper wrapper){
        errorMap.computeIfAbsent(key, k -> new HashSet<>()).add(wrapper);
    }


    public void putAll(String key, Collection<EventOperatorWrapper> wrappers){
        eventMap.computeIfAbsent(key, k -> new HashSet<>()).addAll(wrappers);
    }
}
