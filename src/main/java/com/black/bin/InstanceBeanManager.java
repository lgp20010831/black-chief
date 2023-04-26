package com.black.bin;

import com.black.core.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class InstanceBeanManager {

    private static Collection<InstanceBeanResolver> resolvers = new LinkedBlockingQueue<>();

    static {
        resolvers.add(new BeanFactoryInstanceResolver());
        resolvers.add(new ReflexAndBeanFactoryInstanceResolver());
        resolvers.add(new ReflexInstanceResolver());
        resolvers.add(new InstanceResolver());
    }

    public static <T> T instance(Class<T> type, InstanceType instanceType){
        return instance(type, null, instanceType);
    }

    public static <T> T instance(Class<T> type, Map<String, Object> source, InstanceType instanceType){
        InstanceBeanResolver beanResolver = null;
        for (InstanceBeanResolver resolver : resolvers) {
            if (resolver.support(instanceType)) {
                beanResolver = resolver;
                break;
            }
        }
        Assert.notNull(beanResolver, "unknown resolver type: " + instanceType);
        return beanResolver.instance(type, source);
    }


    public static Collection<InstanceBeanResolver> getResolvers() {
        return resolvers;
    }
}
