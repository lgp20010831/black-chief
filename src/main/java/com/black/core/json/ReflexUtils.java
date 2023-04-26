package com.black.core.json;

import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.query.ClassWrapper;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class ReflexUtils {

    public static Class<?>[] getMethodParamterGenericVals(Parameter parameter){
        Type parameterizedType = parameter.getParameterizedType();
        if (parameterizedType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) parameterizedType).getActualTypeArguments();
            return loads(actualTypeArguments);
        }
        return new Class[0];
    }

    public static Class<?>[] getMethodParamterGenericVals(Method method) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (Type genericParameterType : genericParameterTypes) {
            System.out.println("type:"+genericParameterType);
            //ParameterizedType:表示一种参数化类型，比如Collection<Object>
            if(genericParameterType instanceof ParameterizedType){
                Type[] actualTypeArguments = ((ParameterizedType) genericParameterType).getActualTypeArguments();
                for (Type parameterType : actualTypeArguments) {
                    System.out.println(parameterType);
                }
            }
        }
        return new Class[0];
    }

    public static Class<?>[] getMethodReturnGenericVals(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            return loads(actualTypeArguments);
        }
        return new Class[0];
    }

    public static Class<?>[] loads(Type[] types){

        Class<?>[] classes = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            try {
                String name = types[i].getTypeName();
                int s = name.indexOf("<");
                if (s != -1){
                    name = name.substring(0, s);
                }
                classes[i] = Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("load class fail", e);
            }
        }
        return classes;
    }

    public static Object getValue(Field f, Object obj){
        try {
            return f.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Method method, Object bean, Object[] args){
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != args.length){
            throw new RuntimeException("p.szie != args.size");
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            Object arg = args[i];
            if (arg != null){
                if (!type.isAssignableFrom(arg.getClass())){
                    TypeHandler typeHandler = TypeConvertCache.initAndGet();
                    if (typeHandler != null){
                        args[i] = typeHandler.convert(type, arg);
                    }
                }
            }
        }
        try {
            return method.invoke(bean, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<Method> getMethods(Class<?> type){
        Class<?> primordialClass = BeanUtil.getPrimordialClass(type);
        Set<Class<?>> assignableFromClasses = ClassWrapper.getAssignableFromClasses(primordialClass);
        assignableFromClasses.add(type);
        Set<Method> methods = new HashSet<>();
        for (Class<?> assignableFromClass : assignableFromClasses) {
            for (Method declaredMethod : assignableFromClass.getDeclaredMethods()) {
                if (!declaredMethod.isAccessible()) {
                    declaredMethod.setAccessible(true);
                }
                methods.add(declaredMethod);
            }
        }
        return methods;
    }

    public static Method getMethod(String name, int paramCount, Object bean){
        return getMethod(name, paramCount, BeanUtil.getPrimordialClass(bean));
    }

    public static Method getMethod(String name, int paramCount, Class<?> beanClass){
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    public static Field getField(String fieldName, Object obj){
        return getField(fieldName, obj.getClass());
    }

    public static Field getField(String fieldName, Class<?> objClass){
        Field field;
        try {
            field = objClass.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return field;
    }

    public static void setValue(Field f, Object obj, Object value){
        if (value != null){
            Class<?> type = f.getType();
            Class<?> valType = value.getClass();
            if (!type.isAssignableFrom(valType)){
                if (log.isDebugEnabled()) {
                    //log.debug("need to convert:{} --> {}", valType.getSimpleName(), type.getSimpleName());
                }
                TypeHandler handler = TypeConvertCache.initAndGet();
                if (handler != null){
                    value = handler.convert(type, value);
                }
            }
        }
        try {
            f.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T instance(Class<T> clazz){
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
