package com.black.lock;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.factory.ReusingProxyFactory;

public class LockProxy {

    public static <T> T proxy(Class<T> type){
        return proxy(type, ProxyType.REFLEX);
    }

    public static <T> T proxy(Class<T> type, ProxyType proxyType){
        FactoryManager.init();
        ReusingProxyFactory proxyFactory = FactoryManager.getProxyFactory();
        ShareLockLayer lockLayer = new ShareLockLayer();
        switch (proxyType){
            case BEAN_FACTORY:
                return proxyFactory.proxy(type, lockLayer, FactoryManager.getBeanFactory());
            case INSTANCE_FACTORY:
                return proxyFactory.proxy(type, lockLayer, FactoryManager.getInstanceFactory());
            case REFLEX:
                return proxyFactory.proxy(type, lockLayer);
            default:
                throw new IllegalStateException("ill proxy type: " + proxyType);
        }
    }

}
