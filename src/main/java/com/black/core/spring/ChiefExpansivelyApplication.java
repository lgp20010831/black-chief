package com.black.core.spring;

import com.black.scan.ChiefScanner;
import com.black.core.json.NotNull;
import com.black.core.spring.driver.*;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.InstanceFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

//ChiefExpansivelyApplication
public interface ChiefExpansivelyApplication {

    boolean isLoad();

    void run();

    void load();

    void applicationStart();

    void shutdown();

    boolean isShutdown();

    boolean actAgent();

    void pattern();


    <T> T reusingProxy(Class<T> beanClazz, AgentLayer agentLayer);

    InstanceFactory instanceFactory();

    ChiefScanner obtainScanner();

    ReusingProxyFactory obtainReusingProxyFactory();

    Class<?> getStartUpClazz();

    void setScannerType(Class<? extends ChiefScanner> scannerType);

    <A extends Annotation> A getAnnotationByMainClass(Class<A> type);

    default <A extends Annotation> boolean isPresenceAnnotationByMainClass(Class<A> type){
        return getAnnotationByMainClass(type) != null;
    }

    Collection<Class<?>> getProjectClasses();

    <T extends OpenComponent> T queryComponent(@NotNull Class<T> componentType);

    Object instanceComponent(Class<?> beanClass);

    PettyApplicationConfiguration obtainConfiguration();

    Map<Class<? extends OpenComponent>, Object> obtainOpenComponents();

    Map<Class<? extends Driver>, Collection<Class<? extends Driver>>> obtainDriverInitializationCache();

    Map<Class<? extends Driver>, Collection<Driver>> obtainDriverInstanceMutes();

    Map<Class<?>, Object> getComponentMutes();

    Object registerComponent(Class<? extends OpenComponent> componentClass);

    boolean registerComponentInstance(OpenComponent openComponent);

    Object refuseComponent(Class<?> componentClass);

    Collection<Class<? extends OpenComponent>> obtainEarlyComponentClazzList();

    Collection<ApplicationDriver> obtainApplicationDrivers();

    Collection<FilterComponent> filterComponentMutes();

    Collection<Object> getApplicationConfigurationMutes();

    Collection<PostBeforeBeanInstantiationDriver> postBeforeBeanInstantiationDrivers();

    Collection<PostBeanAfterInitializationDriver> postBeanAfterInitializationDrivers();

    Collection<PostBeanRegisterDriver> postBeanRegisterDrivers();

    Collection<PostBeanFactoryDriver> postBeanFactoryDrivers();

    Collection<PostPatternClazzDriver> postPatternClazzDriverMutes();

    Collection<PostSpringMutesDriver> postSpringMutesDrivers();

    Collection<SortDriver> sortDrivers();

    Collection<LoadComponentDriver> loadComponentDrivers();

    Collection<PostLoadBeanMutesDriver> postLoadBeanMutesDrivers();

    Collection<PostComponentInstance> postComponentInstance();
}
