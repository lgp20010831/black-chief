package com.black.core.spring;

import com.black.core.spring.driver.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDriverCache extends AbstractEmbedApplication {


    protected final Collection<PostPatternClazzDriver> postPatternClazzDriverCollection = new ArrayList<>();

    protected final Map<Class<? extends Driver>, Collection<Class<? extends Driver>>> driverInitializationCache = new HashMap<>();

    protected final Map<Class<? extends Driver>, Collection<Driver>> driverInstanceCache = new HashMap<>();

    public AbstractDriverCache(Object configuration) {
        super(configuration);
    }

    @Override
    public Map<Class<? extends Driver>, Collection<Class<? extends Driver>>> obtainDriverInitializationCache() {
        return driverInitializationCache;
    }

    @Override
    public Map<Class<? extends Driver>, Collection<Driver>> obtainDriverInstanceMutes() {
        return driverInstanceCache;
    }

    public <P extends Driver> Collection<P> getDriver(Class<P> driverClazz){
        return (Collection<P>) driverInstanceCache.computeIfAbsent(driverClazz, d -> new ArrayList<>());
    }

    @Override
    public Collection<ApplicationDriver> obtainApplicationDrivers() {
        return getDriver(ApplicationDriver.class);
    }

    @Override
    public Collection<PostBeanAfterInitializationDriver> postBeanAfterInitializationDrivers() {
        return getDriver(PostBeanAfterInitializationDriver.class);
    }

    @Override
    public Collection<PostBeanRegisterDriver> postBeanRegisterDrivers() {
        return getDriver(PostBeanRegisterDriver.class);
    }

    @Override
    public Collection<PostBeanFactoryDriver> postBeanFactoryDrivers() {
        return getDriver(PostBeanFactoryDriver.class);
    }

    @Override
    public Collection<PostPatternClazzDriver> postPatternClazzDriverMutes() {
        return getDriver(PostPatternClazzDriver.class);
    }

    @Override
    public Collection<PostSpringMutesDriver> postSpringMutesDrivers() {
        return getDriver(PostSpringMutesDriver.class);
    }

    @Override
    public Collection<SortDriver> sortDrivers() {
        return getDriver(SortDriver.class);
    }

    @Override
    public Collection<LoadComponentDriver> loadComponentDrivers() {
        return getDriver(LoadComponentDriver.class);
    }

    @Override
    public Collection<PostLoadBeanMutesDriver> postLoadBeanMutesDrivers() {
        return getDriver(PostLoadBeanMutesDriver.class);
    }

    @Override
    public Collection<PostBeforeBeanInstantiationDriver> postBeforeBeanInstantiationDrivers() {
        return getDriver(PostBeforeBeanInstantiationDriver.class);
    }

    @Override
    public Collection<PostComponentInstance> postComponentInstance() {
        return getDriver(PostComponentInstance.class);
    }

    @Override
    public Collection<FilterComponent> filterComponentMutes() {
        return getDriver(FilterComponent.class);
    }
}
