package com.black.utils;

import com.black.core.util.Av0;
import com.black.core.util.MethodHandlerUtilsByCSDN;
import com.black.core.util.StringUtils;
import org.springframework.aop.SpringProxy;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyUtil {

    public static final String HASHCODE = "hashCode";

    public static final String TO_STRING = "toString";

    public static final String EQUALS = "equals";

    public static final String GET_CLASS = "getClass";

    private static final Map<Method, MethodHandle> handleCache = new ConcurrentHashMap<>();

    public static final String[] OBJECT_METHOD = new String[]{
        "equals", "hashCode", "getClass", "toString", "clone", "notify", "notifyAll",
            "wait", "finalize", "registerNatives"
    };

    public static final Set<String> OBJECT_METHOD_SET = Av0.set(
            "equals", "hashCode", "getClass", "toString", "clone", "notify", "notifyAll",
            "wait", "finalize", "registerNatives"
    );

    public static final Collection<Method> OBJECT_METHODS = new HashSet<>();

    static {
        Method[] methods = Object.class.getMethods();
        OBJECT_METHODS.addAll(Arrays.asList(methods));
    }

    public static boolean isAopProxy(@Nullable Object object) {
        assert object != null;
        return (Proxy.isProxyClass(object.getClass()) ||
                object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
    }

    /**
     * Check whether the given object is a JDK dynamic proxy.
     * <p>This method goes beyond the implementation of
     * {@link Proxy#isProxyClass(Class)} by additionally checking if the
     * given object is an instance of {@link SpringProxy}.
     * @param object the object to check
     * @see Proxy#isProxyClass
     */
    public static boolean isJdkDynamicProxy(@Nullable Object object) {
        assert object != null;
        return Proxy.isProxyClass(object.getClass());
    }

    /**
     * Check whether the given object is a CGLIB proxy.
     * <p>This method goes beyond the implementation of
     * {@link ClassUtils#isCglibProxy(Object)} by additionally checking if
     * the given object is an instance of {@link SpringProxy}.
     * @param object the object to check
     * @see ClassUtils#isCglibProxy(Object)
     */
    public static boolean isCglibProxy(@Nullable Object object) {
        assert object != null;
        return object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR);
    }

    public static <T> Class<T> getPrimordialClass(T obj){
        Class<T> targetClazz;
        if (ProxyUtil.isCglibProxy(obj)) {
            targetClazz = (Class<T>) obj.getClass().getSuperclass();
        }else if (ProxyUtil.isJdkDynamicProxy(obj)){
            targetClazz = (Class<T>) obj.getClass().getInterfaces()[0];
        }else {
            targetClazz = (Class<T>) obj.getClass();
        }
        return targetClazz;
    }

    public static boolean isObjectMethod(String name){
        return OBJECT_METHOD_SET.contains(name);
    }

    public static boolean isObjectMethod(Method method){
        return OBJECT_METHODS.contains(method);
    }

    public static boolean isDefaultMethod(Method method){
        return method.isDefault();
    }

    public static Object invokeDefaultMethod(Method method, Object proxy, Object[] args) throws Throwable {
        MethodHandle handle = handleCache.computeIfAbsent(method, md -> {
            MethodHandle methodHandle = MethodHandlerUtilsByCSDN.getSpecialMethodHandle(method);
            return methodHandle.bindTo(proxy);
        });
        return handle.invokeWithArguments(args);
    }

    public static boolean filterObjectMethod(Method method){
        final String methodName = method.getName();
        for (String name : OBJECT_METHOD) {
            if (name.equals(methodName))
                return true;
        }
       return false;
    }

    public static Object invokeObjectMethod(Method method, Object obj, Object[] args, MethodProxy methodProxy){
        String methodName = method.getName();
        Class<?> targetClass;
        if (isJdkDynamicProxy(obj)) {
            targetClass = obj.getClass().getInterfaces()[0];
        }else if (isCglibProxy(obj)){
            targetClass = obj.getClass().getSuperclass();
        }else {
            targetClass = obj.getClass();
        }

        try {

            if (isOverWriteMethod(method, targetClass, Object.class)) {
                if (methodProxy != null){
                    return methodProxy.invokeSuper(obj, args);
                }else {
                    return null;
                }
            }

            if (methodName.equals(HASHCODE) && (args == null || args.length == 0)){
                List<Field> fields = ReflexHandler.getAccessibleFields(targetClass);
                Object[] hashArgs = new Object[fields.size()];
                for (int i = 0; i < fields.size(); i++) {
                    try {
                        hashArgs[i] = fields.get(i).get(obj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
                return Objects.hash(hashArgs);
            }else if (methodName.equals(EQUALS) && args.length == 1) {
                return obj == args[0];
            }else if (methodName.equals(TO_STRING) && (args == null || args.length == 0)){
                return StringUtils.linkStr(obj.getClass().getName(), "(AgentLayer)@", Integer.toHexString(obj.hashCode()));
            }else {
                return null;
            }
        }catch (Throwable e){
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean isOverWriteMethod(Method method, Class<?> sonClass, Class<?> superClass){
        try {
            Method classDeclaredMethod = sonClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            Method declaredMethod = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
