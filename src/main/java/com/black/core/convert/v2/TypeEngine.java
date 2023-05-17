package com.black.core.convert.v2;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.chain.GroupKeys;
import com.black.core.convert.ConversionWay;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import lombok.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 优化 typeHandler, 保持 typeHandler 特性
 * xxx -> xxx
 * @author 李桂鹏
 * @create 2023-05-16 16:08
 */
@SuppressWarnings("all") @Data
public class TypeEngine {

    private static final List<Predicate<Method>> PREDICATES = new ArrayList<>();

    private static final List<Function<Method, Integer>> getSortFunctions = new ArrayList<>();

    private static TypeEngine engine;

    private BringRearTypeHandler bringRearTypeHandler;

    private final Map<GroupKeys, MethodHandler> duplicateCache = new ConcurrentHashMap<>();

    private final Map<GroupKeys, MethodHandler> cache = new ConcurrentHashMap<>();

    private boolean useCache = true;

    public static synchronized TypeEngine getInstance() {
        if (engine == null){
            engine = new TypeEngine();
        }
        return engine;
    }

    static {
        PREDICATES.add(method -> {
            return method.isAnnotationPresent(ConversionWay.class);
        });

        getSortFunctions.add(method -> {
            ConversionWay annotation = method.getAnnotation(ConversionWay.class);
            return annotation == null ? null : annotation.priority();
        });
    }

    public <T> T convert(Class<T> type, Object value){
        if (value == null){
            return null;
        }

        Class<Object> primordialClass = BeanUtil.getPrimordialClass(value);
        if (type.isAssignableFrom(primordialClass)){
            return (T) value;
        }
        try {
            MethodHandler handler;
            if (useCache){
                GroupKeys groupKeys = new GroupKeys(type, primordialClass);
                handler = cache.computeIfAbsent(groupKeys, gk -> matchHandler(type, value));
            }else {
                handler = matchHandler(type, value);
            }
            return (T) handler.invoke(value);
        }catch (RuntimeException e){
            if (bringRearTypeHandler != null){
                return bringRearTypeHandler.convert(value, type);
            }
            throw e;
        }
    }

    private MethodHandler matchHandler(Class<?> type, Object value){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(value);
        for (MethodHandler methodHandler : duplicateCache.values()) {
            if (methodHandler.supportConvert(type) && methodHandler.supportParam(primordialClass)) {
                return methodHandler;
            }
        }
        throw new IllegalStateException("无法转换 " + primordialClass.getSimpleName() + " --> " + type.getSimpleName());
    }

    public void parseObj(Object instance, boolean force){
        parse(BeanUtil.getPrimordialClass(instance), instance, force);
    }

    public void parseClass(Class<?> source, boolean force){
        boolean forInstance = checkClassForInstance(source);
        parse(source, forInstance ?
                InstanceBeanManager.instance(source, InstanceType.REFLEX_AND_BEAN_FACTORY) :
                null, force);
    }

    public void parse(Class<?> source, Object instance, boolean force){

        ClassWrapper<?> classWrapper = ClassWrapper.get(source);
        Collection<MethodWrapper> methods = classWrapper.getMethods();
        if (!force){
            methods = StreamUtils.filterList(methods, methodWrapper -> {
                for (Predicate<Method> predicate : PREDICATES) {
                    if (predicate.test(methodWrapper.get())){
                        return true;
                    }
                }
                return false;
            });
        }
        for (MethodWrapper methodWrapper : methods) {
            Method method = methodWrapper.get();
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (instance == null){
                //如果实例为空, 则只读取 static 方法
                if (!isStatic){
                    continue;
                }
                //静态方法必须为公开的
                if(!Modifier.isPublic(method.getModifiers())){
                    continue;
                }
            }
            if (!eligible(method)){
                continue;
            }
            Class<?> returnType = method.getReturnType();
            Class<?> parameterType = method.getParameterTypes()[0];
            GroupKeys groupKeys = new GroupKeys(returnType, parameterType);
            if (duplicateCache.containsKey(groupKeys)){
                //进行去重
                MethodHandler oldHandler = duplicateCache.get(groupKeys);
                int oldSort = getSort(oldHandler.getMethod());
                int newSort = getSort(method);
                if (newSort > oldSort){
                    //保留新的
                    duplicateCache.remove(groupKeys);
                }else {
                    continue;
                }
            }

            MethodHandler handler = new MethodHandler(method, isStatic ? null : instance, parameterType);
            duplicateCache.put(groupKeys, handler);
        }

    }

    private int getSort(Method method){
        Integer sort = null;
        for (Function<Method, Integer> function : getSortFunctions) {
            sort = function.apply(method);
            if (sort != null){
                break;
            }
        }
        return sort == null ? 0 : sort;
    }

    //判断方法是否符合规定
    private boolean eligible(Method method){
        int count = method.getParameterCount();
        Class<?> returnType = method.getReturnType();
        return count == 1 && !returnType.equals(void.class);
    }

    private boolean checkClassForInstance(Class<?> clazz){
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPublic(constructor.getModifiers())){
                return true;
            }
        }
        return false;
    }
}
