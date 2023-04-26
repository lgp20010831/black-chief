package com.black.core.util;

import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SetGetUtils {

    private static final Map<Class<?>, Map<String, AccessMethod>> getMethodMutes = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Map<String, AccessMethod>> setMethodMutes = new ConcurrentHashMap<>();

    public static Object invokeGetMethod(@NonNull Field field, Object obj){
        return invokeGetMethod(field.getName(), obj);
    }

    public static Collection<AccessMethod> getSetMethods(Class<?> type){
        return setMethodMutes.computeIfAbsent(type, SetGetUtils::parseClassSetMethod).values();
    }

    public static Collection<AccessMethod> getGetMethods(Class<?> type){
        return getMethodMutes.computeIfAbsent(type, SetGetUtils::parseClassGetMethod).values();
    }

    public static Map<String, Object> getValueMap(Object bean){
        Map<String, Object> map = new HashMap<>();
        if (bean == null) return map;
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        ClassWrapper<Object> wrapper = ClassWrapper.get(primordialClass);
        for (FieldWrapper field : wrapper.getFields()) {
            if (hasGetMethod(field.getField())) {
                map.put(field.getName(), invokeGetMethod(field.getField(), bean));
            }
        }
        return map;
    }

    public static Object invokeGetMethod(@NonNull String fieldName, Object obj){
        MethodWrapper getMethod = getGetMethod(BeanUtil.getPrimordialClass(obj), fieldName);
        if (getMethod != null){
            return getMethod.invoke(obj);
        }
        return null;
    }

    public static void invokeSetMethod(@NonNull Field field, Object value, Object obj){
        invokeSetMethod(field.getName(), value, obj);
    }

    public static void invokeSetMethod(@NonNull String fieldName, Object value, Object obj){
        MethodWrapper setMethod = getSetMethod(BeanUtil.getPrimordialClass(obj), fieldName);
        if (setMethod != null){
            Class<?> type = setMethod.getParameterTypes()[0];
            TypeHandler handler = TypeConvertCache.initAndGet();
            if (handler != null){
                value = handler.convert(type, value);
            }
            setMethod.invoke(obj, value);
        }
    }

    public static boolean hasSetMethod(@NonNull Field field){
        return hasSetMethod(field.getDeclaringClass(), field.getName());
    }

    public static boolean hasSetMethod(@NonNull Class<?> superClass, @NonNull String fieldName){
        return getSetMethod(superClass, fieldName) != null;

    }

    public static MethodWrapper getSetMethod(@NonNull Field field){
        return getSetMethod(field.getDeclaringClass(), field.getName());
    }

    public static MethodWrapper getSetMethod(@NonNull Class<?> superClass, @NonNull String fieldName){
        Map<String, AccessMethod> accessMethodMap = setMethodMutes.computeIfAbsent(superClass, SetGetUtils::parseClassSetMethod);
        String upName = fieldName.toUpperCase();
        if (accessMethodMap.containsKey(upName)) {
            return accessMethodMap.get(upName).method;
        }
        return null;
    }

    public static boolean hasGetMethod(@NonNull Field field){
        return hasGetMethod(field.getDeclaringClass(), field.getName());
    }

    public static boolean hasGetMethod(@NonNull Class<?> superClass, @NonNull String fieldName){
        return getGetMethod(superClass, fieldName) != null;

    }

    public static MethodWrapper getGetMethod(@NonNull Field field){
        return getGetMethod(field.getDeclaringClass(), field.getName());
    }

    public static MethodWrapper getGetMethod(@NonNull Class<?> superClass, @NonNull String fieldName){
        Map<String, AccessMethod> accessMethodMap = getMethodMutes.computeIfAbsent(superClass, SetGetUtils::parseClassGetMethod);
        String upName = fieldName.toUpperCase();
        if (accessMethodMap.containsKey(upName)) {
            return accessMethodMap.get(upName).method;
        }
        return null;
    }

    public static AccessMethod castToGetMethod(MethodWrapper methodWrapper){
        String name = methodWrapper.getName();
        if (isGetMethod(methodWrapper)) {
            String fieldName = name.startsWith("is") ? name.substring(2).toUpperCase() : name.substring(3).toUpperCase();
            String rawFieldName = StringUtils.titleLower(name.startsWith("is") ? name.substring(2) : name.substring(3));
            return new AccessMethod(fieldName, methodWrapper, rawFieldName);
        }
        return null;
    }

    public static boolean isGetMethod(MethodWrapper method){
        String name = method.getName();
        if (name.equals("getClass")){
            return false;
        }
        Class<?> returnType = method.getReturnType();
        return (name.startsWith("get") ||
                (name.startsWith("is") &&
                        (returnType.equals(boolean.class)
                                || returnType.equals(Boolean.class))
                )
        ) && method.getParameterCount() == 0 && !returnType.equals(void.class);
    }

    private static Map<String, AccessMethod> parseClassGetMethod(Class<?> targetClass){
        Map<String, AccessMethod> accessMethodMap = new ConcurrentHashMap<>();
        ClassWrapper<?> wrapper = ClassWrapper.get(targetClass);
        for (MethodWrapper method : wrapper.getMethods()) {
            String name = method.getName();
            if (isGetMethod(method)) {
                String fieldName = name.startsWith("is") ? name.substring(2).toUpperCase() : name.substring(3).toUpperCase();
                String rawFieldName = StringUtils.titleLower(name.startsWith("is") ? name.substring(2) : name.substring(3));
                accessMethodMap.put(fieldName, new AccessMethod(fieldName, method, rawFieldName));
            }
        }
        return accessMethodMap;
    }

    public static AccessMethod castToSetMethod(MethodWrapper methodWrapper){
        String name = methodWrapper.getName();
        if (isSetMethod(methodWrapper)) {
            String fieldName = name.substring(3).toUpperCase();
            String rawFieldName = StringUtils.titleLower(name.substring(3));
            return new AccessMethod(fieldName, methodWrapper, rawFieldName);
        }
        return null;
    }

    public static boolean isSetMethod(MethodWrapper method){
        String name = method.getName();
        return name.startsWith("set") && method.getParameterCount() == 1 && method.getReturnType().equals(void.class);
    }

    private static Map<String, AccessMethod> parseClassSetMethod(Class<?> targetClass){
        Map<String, AccessMethod> accessMethodMap = new ConcurrentHashMap<>();
        ClassWrapper<?> wrapper = ClassWrapper.get(targetClass);
        for (MethodWrapper method : wrapper.getMethods()) {
            String name = method.getName();
            if (isSetMethod(method)){
                //find set method
                String fieldName = name.substring(3).toUpperCase();
                String rawFieldName = StringUtils.titleLower(name.substring(3));
                accessMethodMap.put(fieldName, new AccessMethod(fieldName, method, rawFieldName));
            }
        }
        return accessMethodMap;
    }

    public static Map<Class<?>, Map<String, AccessMethod>> getGetMethodMutes() {
        return getMethodMutes;
    }

    public static Map<Class<?>, Map<String, AccessMethod>> getSetMethodMutes() {
        return setMethodMutes;
    }

    @Getter
    public static class AccessMethod{
        private final String fieldName;
        private final MethodWrapper method;
        private final String rawFieldName;


        public AccessMethod(String fieldName, MethodWrapper method, String rawFieldName) {
            this.fieldName = fieldName;
            this.method = method;
            this.rawFieldName = rawFieldName;
        }
    }
}
