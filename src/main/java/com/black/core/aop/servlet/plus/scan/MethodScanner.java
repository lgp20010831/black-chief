package com.black.core.aop.servlet.plus.scan;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.plus.*;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;import com.black.utils.ReflexHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public final class MethodScanner {

    private final ThreadLocal<Map<Method, PlusMethodWrapper>> plusMethodLocalCache = new ThreadLocal<>();

    private final ThreadLocal<Map<Method, EntryWrapper>> entryLocalCache = new ThreadLocal<>();

    private final Map<Method, QueryWrapperConfiguration> configurationCache = new ConcurrentHashMap<>();

    public MethodScanner(){

    }

    public QueryWrapperConfiguration getConfiguration(Method method){
        return configurationCache.get(method);
    }

    public void scannerMethod(Method method, HttpMethodWrapper httpMethodWrapper){
        Map<Method, PlusMethodWrapper> methodWrapperMap = plusMethodLocalCache.get();
        Map<Method, EntryWrapper> entryWrapperMap = entryLocalCache.get();
        if (methodWrapperMap == null){
            methodWrapperMap = new HashMap<>();
            plusMethodLocalCache.set(methodWrapperMap);
        }
        if(entryWrapperMap == null){
            entryWrapperMap = new HashMap<>();
            entryLocalCache.set(entryWrapperMap);
        }
        if (methodWrapperMap.containsKey(method) && entryWrapperMap.containsKey(method)){
            return;
        }

        doScanMethod(method, httpMethodWrapper);
    }

    public QueryWrapperConfiguration handlerConfiguration(Method method, PlusMethodWrapper plusMethodWrapper){
        QueryWrapperConfiguration configuration = new QueryWrapperConfiguration();
        resolverConfiguration(configuration, plusMethodWrapper);
        configurationCache.put(method, configuration);
        return configuration;
    }

    private void doScanMethod(Method method, HttpMethodWrapper httpMethodWrapper){
        PlusMethodWrapper plusMethodWrapper = new PlusMethodWrapper(httpMethodWrapper);
        plusMethodWrapper.setMethod(method);
        EntryWrapper entryWrapper;
        //做扫描
        wriedPlusWrapper(method, plusMethodWrapper);

        if (plusMethodWrapper.getWrapperIndex() == -1) {
            return;
        }
        entryWrapper = wriedEntry(plusMethodWrapper);

        plusMethodLocalCache.get().put(method, plusMethodWrapper);
        entryLocalCache.get().put(method, entryWrapper);
    }

    private void resolverConfiguration(QueryWrapperConfiguration configuration, PlusMethodWrapper plusMethodWrapper){
        WriedQueryWrapper queryWrapper = plusMethodWrapper.getQueryWrapper();
        if (queryWrapper != null){
            com.black.core.util.AnnotationUtils.loadAttribute(queryWrapper, configuration);
        }
    }

    private EntryWrapper wriedEntry(PlusMethodWrapper plusMethodWrapper){
        Class<?> entryClass = plusMethodWrapper.getEntryClass();
        EntryWrapper entryWrapper = new EntryWrapper(entryClass);
        for (Field field : ReflexHandler.getAccessibleFields(entryClass)) {
            entryWrapper.registerField(field.getName(), field);
        }
        return entryWrapper;
    }

    private void wriedPlusWrapper(Method method, PlusMethodWrapper plusMethodWrapper){
        parseEntityClassAndWrapper(method, plusMethodWrapper);
        try {
            if (plusMethodWrapper.getWrapperIndex() == -1){
                return;
            }
            String name = handlerPointName(plusMethodWrapper);
            boolean autoSearch = !StringUtils.hasText(name);
            Parameter[] parameters = method.getParameters();
            //拿到方法封装
            HttpMethodWrapper httpMethodWrapper = plusMethodWrapper.getHttpMethodWrapper();
            //获取泛型对象
            Class<?> entryClass = plusMethodWrapper.getEntryClass();
            boolean isJson = httpMethodWrapper.isJsonRequest();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if(autoSearch){
                    if (isJson){
                        RequestBody annotation = AnnotationUtils.getAnnotation(parameter, RequestBody.class);
                        if (annotation != null){
                            plusMethodWrapper.setArgIndex(i);
                            plusMethodWrapper.setArgParamter(parameter);
                        }
                    }else {
                        Class<?> type = parameter.getType();
                        if (type.equals(entryClass)){
                            plusMethodWrapper.setArgIndex(i);
                            plusMethodWrapper.setArgParamter(parameter);
                        }
                    }
                }else {
                    String parameterName = parameter.getName();
                    if (name.equals(parameterName)){
                        plusMethodWrapper.setArgIndex(i);
                        plusMethodWrapper.setArgParamter(parameter);
                    }
                }
            }
            handlerAnnotation(method, plusMethodWrapper);
        }finally {
            plusMethodWrapper.end();
        }
    }

    private String handlerPointName(PlusMethodWrapper plusMethodWrapper){
        String name = null;
        Parameter paramter = plusMethodWrapper.getWrapperParamter();
        WriedUpdateWrapper updateWrapper = AnnotationUtils.getAnnotation(paramter, WriedUpdateWrapper.class);
        if (updateWrapper != null){
            name = updateWrapper.value();
            plusMethodWrapper.setQueryWrapper(updateWrapper.queryWrapper());
        }else {
            WriedQueryWrapper queryWrapper = AnnotationUtils.getAnnotation(paramter, WriedQueryWrapper.class);
            if(queryWrapper != null){
                name = queryWrapper.value();
                plusMethodWrapper.setQueryWrapper(queryWrapper);
            }else {
                WriedDeletionWrapper deletionWrapper = AnnotationUtils.getAnnotation(paramter, WriedDeletionWrapper.class);
                if (deletionWrapper != null){
                    name = deletionWrapper.value();
                    plusMethodWrapper.setQueryWrapper(deletionWrapper.queryWrapper());
                }
            }
        }
        plusMethodWrapper.setPointParamName(name);
        return name;
    }

    private void parseEntityClassAndWrapper(Method method, PlusMethodWrapper plusMethodWrapper){
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            WriedWrapper annotation = AnnotationUtils.getAnnotation(parameter, WriedWrapper.class);
            if (annotation != null){

                //找到了目标注解
                Class<?> type = parameter.getType();
                if (!Wrapper.class.isAssignableFrom(type)){
                    throw new RuntimeException("被WriedWrapper标注的字段一定要是Wrapper类型");
                }
                Class<?>[] genericVal = ReflexHandler.getParamterGenericVal(parameter);
                if (genericVal == null || genericVal.length == 0){
                    if (log.isWarnEnabled()) {
                        log.warn("你应该在 Wrapper 上指定明确的泛型");
                    }
                    return;
                }
                Class<?> val = genericVal[0];
                if (val.equals(Object.class)){
                    if (log.isWarnEnabled()) {
                        log.warn("泛型应该明确到具体的实体类, 而不是 Object/?");
                    }
                    return;
                }
                plusMethodWrapper.setEntryClass(val);
                plusMethodWrapper.setWrapperIndex(i);
                plusMethodWrapper.setWrapperParamter(parameter);
                break;
            }
        }
    }

    private void handlerAnnotation(Method method, PlusMethodWrapper plusMethodWrapper){
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Annotation[] annotations = AnnotationUtils.getAnnotations(parameter);
            plusMethodWrapper.registerAnnotation(annotations);
        }
    }

    public PlusMethodWrapper getPlusWrapper(Method method){
        return plusMethodLocalCache.get().get(method);
    }

    public EntryWrapper getEntryWrapper(Method method){
        return entryLocalCache.get().get(method);
    }

    public void clear(Method method){
        Map<Method, PlusMethodWrapper> wrapperMap = plusMethodLocalCache.get();
        wrapperMap.clear();
        Map<Method, EntryWrapper> entryWrapperMap = entryLocalCache.get();
        entryWrapperMap.clear();
    }


    public void remove(){
        plusMethodLocalCache.remove();
        entryLocalCache.remove();
    }

}
