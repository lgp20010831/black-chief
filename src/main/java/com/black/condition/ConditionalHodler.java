package com.black.condition;

import com.black.condition.inter.ConditionalResolver;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ConditionalHodler {

    private static BeanFactory factory;

    private static Map<String, ConditionalEngine> cache = new ConcurrentHashMap<>();

    public static ConditionalEngine obtainEngine(String alias){
        return obtainEngine(null, alias);
    }

    public static ConditionalEngine obtainEngine(Consumer<List<Class<? extends ConditionalResolver>>> consumer, String alias){
        init();
        return cache.computeIfAbsent(alias, a -> {
            return new ConditionalEngine(factory, consumer);
        });
    }

    private static void init(){
        if (factory == null){
            FactoryManager.init();
            factory = FactoryManager.getBeanFactory();
        }
    }

    public static BeanFactory getFactory() {
        init();
        return factory;
    }
}
