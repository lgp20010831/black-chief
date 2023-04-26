package com.black.core.spring;


import com.black.core.spring.driver.*;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.SingletonClazzTree;

import java.lang.reflect.Modifier;
import java.util.*;

public class ComponentScanner implements PostPatternClazzDriver {

    private final Collection<Class<? extends PostPatternClazzDriver>> scannerComponent = new ArrayList<>();

    private final Collection<PostPatternClazzDriver> createdPatterns = new ArrayList<>();

    @Override
    public void postPatternClazz(Class<?> beanClazz,
                                 Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                 ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {

       final PettySpringApplication pettySpringApplication = (PettySpringApplication) chiefExpansivelyApplication;
        if (beanClazz.isInterface() || Modifier.isAbstract(beanClazz.getModifiers()) || beanClazz.isEnum())
            return;
        whetherOtherDriver(pettySpringApplication, beanClazz);

        if (PostPatternClazzDriver.class.isAssignableFrom(beanClazz) && !beanClazz.equals(ComponentScanner.class)){
            registerPatternProgress((Class<? extends PostPatternClazzDriver>) beanClazz);
        }
    }

    @Override
    public void endPattern(Collection<Class<?>> clazzCollection,
                           Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                           ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        try {
            instancePatternDriver(chiefExpansivelyApplication);
            for (Class<?> cla : clazzCollection) {
                for (PostPatternClazzDriver clazzDriver : chiefExpansivelyApplication.postPatternClazzDriverMutes()) {
                    if (!this.equals(clazzDriver) && !createdPatterns.contains(clazzDriver)){
                        clazzDriver.postPatternClazz(cla, springLoadComponent, proxyFactory, chiefExpansivelyApplication);
                    }
                }
            }
            for (PostPatternClazzDriver postPatternClazzDriver : chiefExpansivelyApplication.postPatternClazzDriverMutes()) {
                if (!this.equals(postPatternClazzDriver) && !createdPatterns.contains(postPatternClazzDriver)){
                    postPatternClazzDriver.endPattern(clazzCollection, springLoadComponent, proxyFactory, chiefExpansivelyApplication);
                }
            }
        }finally {
            Collection<PostPatternClazzDriver> pcd = chiefExpansivelyApplication.postPatternClazzDriverMutes();
            listSc: for (Class<? extends PostPatternClazzDriver> com : scannerComponent) {
                for (PostPatternClazzDriver postPatternClazzDriver : pcd) {
                    if (postPatternClazzDriver.getClass().equals(com)){
                        createdPatterns.add(postPatternClazzDriver);
                        continue listSc;
                    }
                }
            }
            scannerComponent.clear();
        }
    }

    private void instancePatternDriver(ChiefExpansivelyApplication application){
        try {
            for (Class<? extends PostPatternClazzDriver> clazz : scannerComponent) {
                Object component = application.instanceComponent(clazz);
                if (component != null){
                    application.postPatternClazzDriverMutes().add((PostPatternClazzDriver) component);
                }
            }
        }catch (Throwable e){
            throw new RuntimeException("实例化 pattern 组件发生异常", e);
        }
    }

    private void registerPatternProgress(Class<? extends PostPatternClazzDriver> patternClazz){
        scannerComponent.removeIf(progressClazz -> progressClazz.isAssignableFrom(patternClazz) || progressClazz.equals(patternClazz));
        scannerComponent.add(patternClazz);
    }

    public void whetherOtherDriver(PettySpringApplication pettySpringApplication, Class<?> beanClazz){
        if (PostLoadBeanMutesDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostLoadBeanMutesDriver.class, beanClazz);
        }

        if (PostBeanRegisterDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostBeanRegisterDriver.class, beanClazz);
        }

        if (PostBeanFactoryDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostBeanFactoryDriver.class, beanClazz);
        }

        if (PostBeforeBeanInstantiationDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostBeforeBeanInstantiationDriver.class, beanClazz);
        }

        if (LoadComponentDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, LoadComponentDriver.class, beanClazz);
        }

        if (PostBeanAfterInitializationDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostBeanAfterInitializationDriver.class, beanClazz);
        }

        if (SortDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, SortDriver.class, beanClazz);
        }

        if (PostSpringMutesDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostSpringMutesDriver.class, beanClazz);
        }

        if (PostComponentInstance.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, PostComponentInstance.class, beanClazz);
        }

        if (FilterComponent.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, FilterComponent.class, beanClazz);
        }

        if (ApplicationDriver.class.isAssignableFrom(beanClazz)){
            registerDriver(pettySpringApplication, ApplicationDriver.class, beanClazz);
        }
    }

    private void registerDriver(PettySpringApplication pettySpringApplication, Class<? extends Driver> targetClazz, Class<?> registerClazz){
        Collection<Class<? extends Driver>> present = pettySpringApplication.obtainDriverInitializationCache()
                .computeIfAbsent(targetClazz, k -> new HashSet<>());
        if (SingletonClazzTree.removeSuperAndJudgeHasEnhanceSon(present, registerClazz)) {
            present.add((Class<? extends Driver>) registerClazz);
        }
    }
}
