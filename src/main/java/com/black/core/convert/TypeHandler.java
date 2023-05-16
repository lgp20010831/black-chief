package com.black.core.convert;


import com.black.core.convert.v2.TypeEngine;
import com.black.core.query.ClassWrapper;
import com.black.core.tools.BeanUtil;
import com.black.utils.MappingKeyHandler;
import com.black.utils.ProxyUtil;
import com.black.utils.ReflexHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j2 @SuppressWarnings("all")
public final class TypeHandler {

    @Setter @Getter
    private boolean enginePriority = true;

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
        TypeEngine engine = TypeEngine.getInstance();
        for (Object obj : objs) {
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(obj);
            if (!isParsed(primordialClass)){
                doParse(obj);
                parseType.add(primordialClass);
            }
            engine.parseObj(obj, false);
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
            Class<?> returnType = method.getReturnType();
            Class<?> parameterType = method.getParameterTypes()[0];
            Set<Class<?>> rts = new HashSet<>();
            rts.add(returnType);
            if(ClassWrapper.isBasic(returnType.getName())){
                rts.add(ClassWrapper.pack(returnType.getName()));
            }

            if (ClassWrapper.isBasicWrapper(returnType.getName())){
                rts.add(ClassWrapper.unpacking(returnType.getSimpleName()));
            }
            for (Class<?> rt : rts) {
                String entry = keyHandler.uniqueIdentification( rt, parameterType);
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
        TypeEngine instance = TypeEngine.getInstance();
        if (isEnginePriority()){
            try {
                return instance.convert(targetType, value);
            }catch (RuntimeException e){
                return convert0(targetType, value);
            }

        }else {
            try {
                return convert0(targetType, value);
            }catch (RuntimeException e){
                return instance.convert(targetType, value);
            }
        }
    }

    private Object convert0(Class<?> targetType, Object value){
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
