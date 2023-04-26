package com.black.utils;

import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Log4j2
public class ReflectionUtils {

    private ReflectionUtils(){}

    private static ClassLoader classLoaderCache;

    public static ClassLoader getCacheClassLoader(){
        return classLoaderCache == null ? Thread.currentThread().getContextClassLoader() : classLoaderCache;
    }

    public static void setClassLoaderCache(ClassLoader classLoaderCache) {
        ReflectionUtils.classLoaderCache = classLoaderCache;
    }

    public static Class<?> loadClass(@NonNull String name){
        ClassLoader classLoader = getCacheClassLoader();
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (log.isErrorEnabled()) {
                log.error("not found class: " + name);
            }
            throw new IllegalStateException(e);
        }
    }

    public static ClassWrapper<?> loadClassWrapper(String name){
        return ClassWrapper.get(loadClass(name));
    }

    public static <T> Class<T> findRawClass(@NonNull T instance){
        return BeanUtil.getPrimordialClass(instance);
    }

    public static <T> ClassWrapper<T> findRawClassWrapper(@NonNull T instance){
        return ClassWrapper.get(findRawClass(instance));
    }

    private static ClassWrapper<?> getClassWrapper(Object obj){
        if (obj instanceof ClassWrapper){
            return (ClassWrapper<?>) obj;
        }
        Class<?> clazz;
        if (obj instanceof Class){
            clazz = (Class<?>) obj;
        }else {
            clazz = findRawClass(obj);
        }
        return ClassWrapper.get(clazz);
    }

    public static List<Class<?>> getUseSupperClasses(Class<?> type){
        List<Class<?>> result = new ArrayList<>();
        Class<?> target = type;
        while (!Object.class.equals(target)){
            result.add(target);
            target = target.getSuperclass();
        }
        return result;
    }

    public static Collection<FieldWrapper> getFieldWrappers(Object obj){
        ClassWrapper<?> classWrapper = getClassWrapper(obj);
        return classWrapper.getFields();
    }

    public static Collection<Field> getFields(Object obj){
        return StreamUtils.mapList(getFieldWrappers(obj), FieldWrapper::get);
    }

    public static Collection<FieldWrapper> getStrategyFieldWrappers(Object obj, Predicate<? super FieldWrapper> predicate){
        Collection<FieldWrapper> fieldWrappers = getFieldWrappers(obj);
        return StreamUtils.filterList(fieldWrappers, predicate);
    }

    public static Collection<Field> getStrategyFields(Object obj, Predicate<? super Field> predicate){
        Collection<Field> fields = getFields(obj);
        return StreamUtils.filterList(fields, predicate);
    }

    public static Collection<MethodWrapper> getMethodWrappers(Object obj){
        ClassWrapper<?> classWrapper = getClassWrapper(obj);
        return classWrapper.getMethods();
    }

    public static Collection<Method> getMethods(Object obj){
        return StreamUtils.mapList(getMethodWrappers(obj), MethodWrapper::get);
    }

    public static Collection<MethodWrapper> getStrategyMethodWrappers(Object obj, Predicate<? super MethodWrapper> predicate){
        Collection<MethodWrapper> methodWrappers = getMethodWrappers(obj);
        return StreamUtils.filterList(methodWrappers, predicate);
    }

    public static Collection<Method> getStrategyMethods(Object obj, Predicate<? super Method> predicate){
        Collection<Method> methods = getMethods(obj);
        return StreamUtils.filterList(methods, predicate);
    }

    //获取方法参数的泛型类型
    public static Class<?>[] getMethodParamterGenericVals(Parameter parameter){
        Type parameterizedType = parameter.getParameterizedType();
        if (parameterizedType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) parameterizedType).getActualTypeArguments();
            return loads(actualTypeArguments);
        }
        return new Class[0];
    }

    //获取方法所有参数的泛型参数
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

    //获取方法返回值泛型类型
    public static Class<?>[] getMethodReturnGenericVals(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            return loads(actualTypeArguments);
        }
        return new Class[0];
    }

    public static Class<?>[] genericVal(Field field, Class<?> achieveInterface){
        if (field == null)
            return new Class[0];
        Type genericType = field.getGenericType();
        if(achieveInterface != null && !achieveInterface.isInterface()){
            return new Class[0];
        }
        if (genericType instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            if (achieveInterface != null && !(load(parameterizedType.getRawType())).equals(achieveInterface))
                return new Class[0];
            return loads(parameterizedType.getActualTypeArguments());
        }else if (genericType instanceof Class){
            return genericVal((Class<?>) genericType, achieveInterface);
        }

        return new Class[0];
    }

    public static Class<?>[] loopUpGenerics(Type genericType, Class<?> achieveInterface){
        if (genericType instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            if (achieveInterface != null && !(load(parameterizedType.getRawType())).equals(achieveInterface))
                return new Class[0];
            return loads(parameterizedType.getActualTypeArguments());
        }else if (genericType instanceof Class){
            return genericVal((Class<?>) genericType, achieveInterface);
        }
        return new Class[0];
    }

    /***
     * 获取指定类实现的某个接口上的泛型
     * @param targetClass 要检查的类
     * @param achieveInterface 指定该接口, 检查该类实现该接口的泛型
     * @return 返回泛型 class 对象
     */
    public static Class<?>[] genericVal(Class<?> targetClass, Class<?> achieveInterface){

        if (targetClass == null)
            return new Class[0];
        Type[] genericInterfaces = targetClass.getGenericInterfaces();
        if(achieveInterface != null && !achieveInterface.isInterface()){
            if (log.isWarnEnabled()){
                log.warn("achieve interface must be interface");
            }
            return new Class[0];
        }
        for (Type genericInterface : genericInterfaces) {
            ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
            if (achieveInterface != null && !(load(parameterizedType.getRawType())).equals(achieveInterface))
                continue;
            return loads(parameterizedType.getActualTypeArguments());
        }
        return new Class[0];
    }

    /* 向上寻找存在泛型的父类  */
    public static Class<?>[] loopSuperGenericVal(Class<?> targetClass, int size){
        Class<?> type = targetClass;
        for (;;){
            Class<?>[] genericVal = superGenericVal(type);
            if (genericVal.length != size){
                Class<?> superclass = type.getSuperclass();
                if (superclass != null ){
                    type = type.getSuperclass();
                    continue;
                }else {
                    throw new IllegalStateException("error for extends AutoMapperController:" + type);
                }
            }
            return genericVal;
        }
    }
    /** 获取父类泛型 */
    public static Class<?>[] superGenericVal(Class<?> targetClass){
        if (targetClass == null)
            return new Class[0];
        Type genericSuperclass = targetClass.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType))
        {
            if (log.isDebugEnabled()){
                log.warn("target not implemented generic super class");
            }
            return new Class[0];
        }
        return loads(((ParameterizedType) genericSuperclass).getActualTypeArguments());
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
                throw new IllegalStateException("load class fail", e);
            }
        }
        return classes;
    }

    public static Class<?> load(@NotNull Type type){
        try {
            return Class.forName(type.getTypeName());
        } catch (ClassNotFoundException e) {
            if (log.isErrorEnabled()){
                log.error("load class fail {}", e.getMessage());
            }
            throw new IllegalStateException("fail to load class:" + type, e);
        }
    }

    public static <T> T instanceByType(Class<T> type){
        ClassWrapper<?> classWrapper = getClassWrapper(type);
        return (T) classWrapper.instance();
    }

    public static Object instance(Object obj){
        ClassWrapper<?> classWrapper = getClassWrapper(obj);
        return classWrapper.instance();
    }

}
