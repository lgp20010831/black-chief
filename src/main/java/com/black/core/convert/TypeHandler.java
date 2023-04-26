package com.black.core.convert;


import com.black.core.tools.BeanUtil;
import com.black.utils.MappingKeyHandler;
import com.black.utils.ProxyUtil;
import com.black.utils.ReflexHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j2
public final class TypeHandler {

    private final MappingKeyHandler keyHandler;

    private final Map<String, TypeMethodWrapper> entryWithMethod = new ConcurrentHashMap<>();

    public TypeHandler(MappingKeyHandler keyHandler) {
        this.keyHandler = keyHandler;
    }

    private Set<Class<?>> parseType = new HashSet<>();

    public void parseSingle(Object obj){
        parse(Arrays.asList(obj));
    }

    public void parse(Collection<Object> objs){
        for (Object obj : objs) {
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(obj);
            if (!isParsed(primordialClass)){
                doParse(obj);
                parseType.add(primordialClass);
            }
        }
    }

    public boolean isParsed(Class<?> type){
        return parseType.contains(type);
    }

    private void doParse(Object obj){
        //获取所有的方法
        List<Method> methods = ReflexHandler.getAccessibleMethods(obj);
        Set<Method> collect = methods.stream().filter(this::filterMethod).collect(Collectors.toSet());
        for (Method method : collect) {
            String entry = keyHandler.uniqueIdentification( method.getReturnType(), method.getParameterTypes()[0]);
            if (entryWithMethod.containsKey(entry)) {
                TypeMethodWrapper wrapper = entryWithMethod.get(entry);
                if (save(method, wrapper.getMethod())){
                    entryWithMethod.remove(entry);
                }else {
                    if (log.isDebugEnabled()) {
                        log.debug("filter low priority method:{}", method);
                    }
                    continue;
                }
            }
            TypeMethodWrapper typeMethodWrapper = new TypeMethodWrapper(method, obj, entry);
            entryWithMethod.put(entry, typeMethodWrapper);
        }
    }

    private boolean save(Method sourceMethod, Method targetMethod){
        ConversionWay way1 = AnnotationUtils.getAnnotation(sourceMethod, ConversionWay.class);
        ConversionWay way2 = AnnotationUtils.getAnnotation(targetMethod, ConversionWay.class);
        int priority1 = way1.priority();
        int priority2 = way2.priority();
        return priority1 > priority2;
    }

    private boolean filterMethod(Method method){
        Class<?> returnType;
        return AnnotationUtils.getAnnotation(method, ConversionWay.class) != null &&
                !(returnType = method.getReturnType()).equals(Void.class) &&
                method.getParameterCount() == 1 &&
                !returnType.equals(method.getParameterTypes()[0]);
    }

    public <T> T genericConvert(Class<T> targetType, Object val){
        return (T) convert(targetType, val);
    }

    //targetType: 要转换的类型, value 值
    public Object convert(Class<?> targetType, Object value){
        if (value == null){
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())){
            return value;
        }
        Class<Object> primordialClass = ProxyUtil.getPrimordialClass(value);
        //生成条目
        String[] entries = keyHandler.uniqueIdentificationSupers(targetType, primordialClass);
        for (String entry : entries) {
            if (entryWithMethod.containsKey(entry)){
                return entryWithMethod.get(entry).invoke(value);
            }
        }
        throw new IllegalStateException("无法转换" + primordialClass.getSimpleName() + " --> " + targetType.getSimpleName());
    }
}
