package com.black.bin;

import com.black.core.query.ClassWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.utils.ServiceUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class ApplyProxyFactory {


    private static ClassLoader loader;


    private static final Map<Class<?>, ProxyMetadata> PROXY_METADATA_MAP = new ConcurrentHashMap<>();

    public static void setLoader(ClassLoader loader) {
        ApplyProxyFactory.loader = loader;
    }

    public static Map<Class<?>, ProxyMetadata> getProxyMetadataMap() {
        return PROXY_METADATA_MAP;
    }

    public static ClassLoader getLoader() {
        return loader == null ? Thread.currentThread().getContextClassLoader() : loader;
    }

    public static <T> ApplyProxy<T> proxyToCast(T bean, ApplyProxyLayer proxyLayer){
        return (ApplyProxy<T>) proxy(bean, proxyLayer);
    }

    public static Class<?> getUserClass(Object bean){
        if (!isProxy(bean)){
            return BeanUtil.getPrimordialClass(bean);
        }else {
            ApplyProxy<?> proxy = (ApplyProxy<?>) bean;
            return getUserClass(proxy.getThis());
        }
    }

    public static <T> T proxy(T bean, ApplyProxyLayer proxyLayer){
        return proxy(bean, proxyLayer, false);
    }

    public static <T> T proxy(T bean, ApplyProxyLayer proxyLayer, boolean regular){
        Assert.notNull(bean, "proxy bean is null");
        Class<?> primordialClass = getUserClass(bean);
        if (primordialClass.isEnum()) {
            throw new IllegalStateException("can not proxy enum");
        }
        if (regular && PROXY_METADATA_MAP.containsKey(primordialClass)){
            ProxyMetadata proxyMetadata = PROXY_METADATA_MAP.get(primordialClass);
            CommonProxyHandler proxyHandler = proxyMetadata.getProxyHandler();
            proxyHandler.addLayer(proxyLayer);
            return bean;
        }else {
            return proxy0(bean, proxyLayer, primordialClass);
        }

    }

    public static boolean isCanCglibProxyOrRegular(Object bean, boolean regular){
        Class<?> primordialClass = getUserClass(bean);
        if (regular && PROXY_METADATA_MAP.containsKey(primordialClass)){
            return true;
        }

        return !primordialClass.isInterface() && qualified(primordialClass);
    }

    private static <T> T proxy0(T bean, ApplyProxyLayer proxyLayer, Class<?> primordialClass){
        CommonProxyHandler handler = new CommonProxyHandler(bean, proxyLayer);
        T proxy;
        if (primordialClass.isInterface()){
            proxy = (T) proxyJdk(primordialClass, handler);
        }else {
            if (qualified(primordialClass)){
                proxy = (T) proxyCGLIB(primordialClass, handler);
            }else {
                proxy = (T) proxyJdk(primordialClass, handler);
            }
        }
        if (!PROXY_METADATA_MAP.containsKey(primordialClass)){
            ProxyMetadata proxyMetadata = new ProxyMetadata(proxy, handler);
            PROXY_METADATA_MAP.put(primordialClass, proxyMetadata);
        }
        return proxy;
    }

    public static Object proxyJdk(Class<?> primordialClass, CommonProxyHandler handler){
        Class<?>[] interfaceArray;
        if (primordialClass.isInterface()){
            interfaceArray = new Class[]{primordialClass, ApplyProxy.class};
        }else {
            ClassWrapper<?> wrapper = ClassWrapper.get(primordialClass);
            Set<Class<?>> interfaces = wrapper.getInterfaces();
            interfaces.add(ApplyProxy.class);
            interfaceArray = interfaces.toArray(new Class[0]);
        }
        return Proxy.newProxyInstance(getLoader(), interfaceArray, handler);
    }

    public static Object proxyCGLIB(Class<?> primordialClass, CommonProxyHandler handler, Class<?>... superInterfaces){
        return proxyCGLIB(primordialClass, handler, false, superInterfaces);
    }

    public static Object proxyCGLIB(Class<?> primordialClass, CommonProxyHandler handler, boolean useNewConstructions, Class<?>... superInterfaces){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(primordialClass);
        enhancer.setClassLoader(getLoader());
        Class<?>[] interfaces = new Class[superInterfaces.length + 1];
        interfaces[0] = ApplyProxy.class;
        for (int i = 0; i < superInterfaces.length; i++) {
            interfaces[i + 1] = superInterfaces[i];
        }
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackType(CommonProxyHandler.class);
        if (!useNewConstructions && existenceParameterlessConstructions(primordialClass)){
            enhancer.setCallback(handler);
            return enhancer.create();
        }else {
            return instanceObject(enhancer, handler);
        }
    }

    public static Object instanceObject(Enhancer enhancer, CommonProxyHandler handler){
        //enhancer.setInterceptDuringConstruction(false);
        Class enhancerClass = enhancer.createClass();
        ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
        Constructor<Object> javaLangConstructor = getJavaLangConstructor();
        Constructor<?> constructor = factory.newConstructorForSerialization(enhancerClass, javaLangConstructor);
        try {
            Object proxyInstance = constructor.newInstance();
            ((Factory)proxyInstance).setCallback(0, handler);
            return proxyInstance;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static Constructor<Object> getJavaLangConstructor(){
        try {
            return Object.class.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean existenceParameterlessConstructions(Class<?> type){
        try {
            type.getConstructor();
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    public static boolean qualified(Class<?> target){
        int modifiers = target.getModifiers();
        if (Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)){
            return false;
        }
        return true;
    }



    public static boolean isProxy(Object bean){
        return bean instanceof ApplyProxy;
    }

}
