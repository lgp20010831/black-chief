package com.black.core.aop.servlet.item;

import com.black.core.util.StreamUtils;

import java.util.Collection;
import java.util.HashSet;

public class ThreadLocalItemResolver extends ItemResolver{

    private final Collection<Class<? extends LAOperatorProcessor>> processorClasses = new HashSet<>();

    private final ThreadLocal<Collection<LAOperatorProcessor>> processorLocal = new ThreadLocal<>();

    private final InstanceProcessor instanceProcessor;

    public ThreadLocalItemResolver(InstanceProcessor instanceProcessor) {
        this.instanceProcessor = instanceProcessor;
    }

    @FunctionalInterface
    public interface InstanceProcessor{
        LAOperatorProcessor instance(Class<? extends LAOperatorProcessor> clazz);
    }

    @Override
    public void addAll(Collection<LAOperatorProcessor> operatorProcessors) {
        throw new UnsupportedOperationException();
    }

    public void add(Class<? extends LAOperatorProcessor> processorClass){
        if (processorClass != null){
            processorClasses.add(processorClass);
        }
    }

    public void addAll0(Collection<Class<? extends LAOperatorProcessor>> processorClasses){
        if (processorClasses != null){
            this.processorClasses.addAll(processorClasses);
        }
    }

    @Override
    protected Collection<LAOperatorProcessor> obtainProcessors() {
        Collection<LAOperatorProcessor> laOperatorProcessors = processorLocal.get();
        if (laOperatorProcessors == null){
            laOperatorProcessors = StreamUtils.mapList(processorClasses, instanceProcessor::instance);
            processorLocal.set(laOperatorProcessors);
        }
        return laOperatorProcessors;
    }
}
