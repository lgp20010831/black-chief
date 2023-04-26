package com.black.spring.agency;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.callback.SpringApplicationHodler;
import com.black.holder.SpringHodler;
import com.black.pattern.LazyBean;
import com.black.pattern.MethodInvoker;
import com.black.spring.ChiefSpringApplication;
import com.black.spring.ChiefSpringHodler;
import com.black.core.chain.GroupKeys;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("all")
public class AgencyScanClass {

    public static boolean cancel = false;

    public static void setCancel(boolean cancel) {
        AgencyScanClass.cancel = cancel;
    }

    public static void present(Class<?> type){
        if (cancel){
            return;
        }
        if (ignoreAnnotation() || annotationWith(type)){
            parseClass(type);
        }
    }

    private static boolean ignoreAnnotation(){
        SpringApplication application = SpringApplicationHodler.getSpringApplication();
        if (application instanceof ChiefSpringApplication){
            return ((ChiefSpringApplication) application).isDetailedInventory();
        }
        return false;
    }

    private static boolean annotationWith(Class<?> type){
        return type.isAnnotationPresent(Agency.class);
    }

    private static void parseClass(Class<?> type){
        Agency annotation = type.getAnnotation(Agency.class);
        ClassWrapper<?> cw = ClassWrapper.get(type);
        Object instance = instanceBean(type, annotation == null || annotation.lazy());
        List<MethodWrapper> rewriteMethods = cw.getMethodByAnnotation(Rewrite.class);
        handlerRewriteMethods(rewriteMethods, type, instance);

        List<MethodWrapper> byAnnotationMethods = cw.getMethodByAnnotation(RewriteOnAnnotation.class);
        handlerRewriteOnAnnotationMethods(byAnnotationMethods, type, instance);

        List<MethodWrapper> customMethods = cw.getMethodByAnnotation(RewriteOnCustom.class);
        handlerRewriteOnCustomMethods(customMethods, type, instance);
    }

    private static void handlerRewriteOnCustomMethods(List<MethodWrapper> rewriteMethods, Class<?> agencyClass,
                                                          Object instance){
        for (MethodWrapper methodWrapper : rewriteMethods) {
            parseRewriteOnCustomMethod(agencyClass, methodWrapper, instance);
        }
    }

    private static void handlerRewriteOnAnnotationMethods(List<MethodWrapper> rewriteMethods, Class<?> agencyClass,
                                              Object instance){
        for (MethodWrapper methodWrapper : rewriteMethods) {
            parseRewriteOnAnnotationMethod(agencyClass, methodWrapper, instance);
        }
    }

    private static void handlerRewriteMethods(List<MethodWrapper> rewriteMethods, Class<?> agencyClass,
                                              Object instance){
        for (MethodWrapper methodWrapper : rewriteMethods) {
            parseRewriteMethod(agencyClass, methodWrapper, instance);
        }
    }

    public static Object instanceBean(Class<?> type, boolean lazy){
        if (lazy){
            return new LazyBean(() -> doInstanceBean(type));
        }else {
            return doInstanceBean(type);
        }
    }

    protected static Object doInstanceBean(Class<?> type){
        Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
        DefaultListableBeanFactory beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        if (beanFactory == null){
            beanFactory = SpringHodler.getListableBeanFactory();
        }
        if (beanFactory != null){
            beanFactory.autowireBean(instance);
        }
        return instance;
    }


    private static void parseRewriteMethod(Class<?> type, MethodWrapper methodWrapper, Object instance){
        ObjectAgencyRegister register = ObjectAgencyRegister.getInstance();
        Rewrite annotation = methodWrapper.getAnnotation(Rewrite.class);
        Class<?> target = annotation.target();
        ClassWrapper<?> targetClassWrapper = ClassWrapper.get(target);
        register.registerSupport(target);
        String name = annotation.name();
        if (!StringUtils.hasText(name)){
            name = methodWrapper.getName();
        }
        MethodWrapper singleMethod = targetClassWrapper.getSingleMethod(name);
        if (singleMethod == null){
            throw new IllegalStateException("can not find method: " + name + " in class: " + type);
        }
        MethodInvoker invoker = new MethodInvoker(methodWrapper);
        invoker.setInvokeBean(instance);
        register.register(new GroupKeys(target, singleMethod.getMethod()), invoker);
    }

    private static void parseRewriteOnAnnotationMethod(Class<?> type, MethodWrapper methodWrapper, Object instance){
        ObjectAgencyRegister register = ObjectAgencyRegister.getInstance();
        RewriteOnAnnotation annotation = methodWrapper.getAnnotation(RewriteOnAnnotation.class);
        Class<? extends Annotation>[] annotationTypes = annotation.value();
        register.setProxyAll(true);
        MethodInvoker invoker = new MethodInvoker(methodWrapper);
        invoker.setInvokeBean(instance);
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            register.registerFunction(new TemporaryDecisionsMethodAgency() {
                @Override
                public boolean capable(Method method) {
                    return AnnotationUtils.isPertain(method, annotationType);
                }
            }, invoker);
        }
    }

    private static void parseRewriteOnCustomMethod(Class<?> type, MethodWrapper methodWrapper, Object instance){
        ObjectAgencyRegister register = ObjectAgencyRegister.getInstance();
        RewriteOnCustom annotation = methodWrapper.getAnnotation(RewriteOnCustom.class);
        Class<? extends RewriteCustomSelector> value = annotation.value();
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        RewriteCustomSelector customSelector = beanFactory.getSingleBean(value);
        register.setProxyAll(true);
        MethodInvoker invoker = new MethodInvoker(methodWrapper);
        invoker.setInvokeBean(instance);
        register.registerFunction(new TemporaryDecisionsMethodAgency() {
            @Override
            public boolean capable(Method method) {
                return customSelector.select(method);
            }
        }, invoker);
    }

}
