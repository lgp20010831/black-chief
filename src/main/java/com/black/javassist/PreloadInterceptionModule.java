package com.black.javassist;

import com.black.aop.ClassInterceptCondition;
import com.black.aop.InterceptFlag;
import com.black.aop.MethodInterceptCondition;
import com.black.aop.impl.CommonInterceptConditionHandler;
import com.black.aop.impl.InterceptOnAnnotationHandler;
import com.black.aop.impl.ResolveIntercetConditionHandler;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.annotation.Sort;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Av0;
import com.black.function.Function;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all")
public class PreloadInterceptionModule {


    private static final LinkedBlockingQueue<ResolveIntercetConditionHandler> handlers = new LinkedBlockingQueue<>();

    static {
        handlers.add(new CommonInterceptConditionHandler());
        handlers.add(new InterceptOnAnnotationHandler());
    }

    public static void load(Set<String> prepareRanges,String... weaverRanges){
        ChiefScanner scanner = ScannerManager.getScanner();
        Set<String> classNames = new HashSet<>();
        for (String weaverRange : weaverRanges) {
            List<String> nameList = scanner.fileNameList(weaverRange);
            for (String name : nameList) {
                if (name.endsWith(".class")){
                    classNames.add(WeaverManager.getClassName(name));
                }
            }
        }
        loadByClassName(prepareRanges, classNames);
    }

    public static void loadByClassName(Set<String> prepareRanges, String... classNames){
        loadByClassName(prepareRanges, Av0.set(classNames));
    }

    public static void loadByClassName(Set<String> prepareRanges, Set<String> classNames){
        Function<CtMethod, Collection<Weaver>> function = handlerWeavers(classNames);
        for (String prepareRange : prepareRanges) {
            WeaverManager.loadClasses(prepareRange, function);
        }
    }

    protected static Function<CtMethod, Collection<Weaver>> handlerWeavers(Set<String> classNames){
        ChiefScanner scanner = ScannerManager.getScanner();
        List<PreloadMethodWrapperWeaver> wrapperWeavers = new ArrayList<>();
        for (String className : classNames) {
            PartiallyCtClass partiallyCtClass = PartiallyCtClass.load(className);
            Set<Class<? extends Annotation>> annptationTypes = partiallyCtClass.getAnnptationTypes();
            if (!annptationTypes.contains(PreloadWeaver.class)){
                continue;
            }
            Class<?> loadClass = ReflectionUtils.loadClass(className);
            List<PreloadMethodWrapperWeaver> preloadMethodWrapperWeavers = handlerWeaver(loadClass);
            wrapperWeavers.addAll(preloadMethodWrapperWeavers);
        }
        return new Function<CtMethod, Collection<Weaver>>() {
            @Override
            public Collection<Weaver> apply(CtMethod ctMethod) throws Throwable {
                CtClass ctClass = ctMethod.getDeclaringClass();
                List<Weaver> weavers = new ArrayList<>();
                for (PreloadMethodWrapperWeaver wrapperWeaver : wrapperWeavers) {
                    if (wrapperWeaver.match(ctMethod, ctClass)) {
                        weavers.add(wrapperWeaver);
                    }
                }
                return ServiceUtils.sort(weavers, weaver -> {
                    PreloadMethodWrapperWeaver preloadMethodWrapperWeaver = (PreloadMethodWrapperWeaver) weaver;
                    return preloadMethodWrapperWeaver.getSort();
                }, false);
            }
        };

    }



    protected static List<PreloadMethodWrapperWeaver> handlerWeaver(Class<?> target){
        Object instance = InstanceBeanManager.instance(target, InstanceType.BEAN_FACTORY_SINGLE);
        ClassWrapper<?> classWrapper = ClassWrapper.get(target);
        Collection<MethodWrapper> methods = classWrapper.getMethods();
        List<PreloadMethodWrapperWeaver> preloadMethodWrapperWeavers = new ArrayList<>();
        for (MethodWrapper methodWrapper : methods) {
            PreloadMethodWrapperWeaver preloadMethodWrapperWeaver = loadMethod(methodWrapper, instance);
            if (preloadMethodWrapperWeaver != null){
                preloadMethodWrapperWeavers.add(preloadMethodWrapperWeaver);
            }
        }
        return preloadMethodWrapperWeavers;
    }

    protected static PreloadMethodWrapperWeaver loadMethod(MethodWrapper methodWrapper, Object instance){
        if (Modifier.isStatic(methodWrapper.getMethod().getModifiers())){
            return null;
        }
        if (!AnnotationUtils.isPertain(methodWrapper.get(), InterceptFlag.class)){
            return null;
        }
        ClassInterceptCondition classInterceptCondition = new ClassInterceptCondition();
        MethodInterceptCondition methodInterceptCondition = new MethodInterceptCondition();

        for (ResolveIntercetConditionHandler handler : handlers) {
            handler.resolveClassCondition(classInterceptCondition, methodWrapper.getMethod());
            handler.resolveMethodCondition(methodInterceptCondition, methodWrapper.getMethod());
        }
        PreloadMethodWrapperWeaver preloadMethodWrapperWeaver = new PreloadMethodWrapperWeaver(methodWrapper.get(), instance, classInterceptCondition, methodInterceptCondition);
        Sort annotation = methodWrapper.getAnnotation(Sort.class);
        if (annotation != null){
            preloadMethodWrapperWeaver.setSort(annotation.value());
        }
        return preloadMethodWrapperWeaver;
    }
}
