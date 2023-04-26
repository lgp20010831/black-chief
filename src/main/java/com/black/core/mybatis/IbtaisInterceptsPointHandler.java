package com.black.core.mybatis;

import com.black.core.spring.instance.InstanceFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.HashMap;
import java.util.Map;

public class IbtaisInterceptsPointHandler {

    private MybatisInterceptsDispatcher mybatisInterceptsDispatcher;

    private LayerWrapper dispatcherWrapper;

    private MybatisInterceptsConfiguartion mybatisInterceptsConfiguartion;

    private final Map<Class<? extends IbatisIntercept>, String[]> earlyIbatisInterceptCache = new HashMap<>();

    private final Map<String, MybatisLayerQueue> mybatisLayers = new HashMap<>();

    public IbtaisInterceptsPointHandler(){
        this(null, null);
    }

    public IbtaisInterceptsPointHandler(MybatisInterceptsDispatcher mybatisInterceptsDispatcher,
                                        MybatisInterceptsConfiguartion mybatisInterceptsConfiguartion) {
        this.mybatisInterceptsDispatcher = mybatisInterceptsDispatcher;
        this.mybatisInterceptsConfiguartion = mybatisInterceptsConfiguartion;
    }

    public void registerEarlyIntercept(Class<? extends IbatisIntercept> ibatisClazz, String[] alias){
        if (!earlyIbatisInterceptCache.containsKey(ibatisClazz)){
            earlyIbatisInterceptCache.put(ibatisClazz, alias);
        }
    }

    public void setMybatisInterceptsConfiguartion(MybatisInterceptsConfiguartion mybatisInterceptsConfiguartion) {
        this.mybatisInterceptsConfiguartion = mybatisInterceptsConfiguartion;
    }

    public void setMybatisInterceptsDispatcher(MybatisInterceptsDispatcher mybatisInterceptsDispatcher) {
        this.mybatisInterceptsDispatcher = mybatisInterceptsDispatcher;
    }

    public void instanceLayers(DefaultListableBeanFactory beanFactory,
                                  InstanceFactory instanceFactory){
        if (mybatisInterceptsDispatcher != null){
            for (Class<? extends IbatisIntercept> clazz : earlyIbatisInterceptCache.keySet()) {
                IbatisIntercept instance;
                try {
                    instance = beanFactory.getBean(clazz);
                }catch (BeansException e){
                    instance = instanceFactory.getInstance(clazz);
                }
                String[] aliases = earlyIbatisInterceptCache.get(clazz);
                for (String alias :aliases) {
                    registerLayer(alias, createLayerWrapper(instance, aliases));
                }
            }
        }
    }

    protected LayerWrapper createLayerWrapper(IbatisIntercept ibatisIntercept, String[] aliases){
        return new DefaultLayerWrapper(aliases, ibatisIntercept);
    }

    protected void registerLayer(String alias, LayerWrapper layerWrapper){
        if (dispatcherWrapper == null){
            dispatcherWrapper = createLayerWrapper(mybatisInterceptsDispatcher, this.mybatisInterceptsConfiguartion.getAliases());
        }
        MybatisLayerQueue layerWrappers = mybatisLayers.computeIfAbsent(alias, k -> new MybatisLayerQueue(dispatcherWrapper));
        layerWrappers.add(layerWrapper);
    }

    public LayerWrapper getDispatcherWrapper() {
        return dispatcherWrapper;
    }

    public Map<Class<? extends IbatisIntercept>, String[]> getEarlyIbatisInterceptCache() {
        return earlyIbatisInterceptCache;
    }

    public Map<String, MybatisLayerQueue> getMybatisLayers() {
        return mybatisLayers;
    }


    public MybatisInterceptsConfiguartion getMybatisInterceptsConfiguartion() {
        return mybatisInterceptsConfiguartion;
    }

    public MybatisInterceptsDispatcher getMybatisInterceptsDispatcher() {
        return mybatisInterceptsDispatcher;
    }
}
