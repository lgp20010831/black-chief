package com.black.scan;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.tools.BeanUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScannerManager {

    private static ChiefScanner instance;

    private static Map<Class<? extends ChiefScanner>, ChiefScanner> cache = new ConcurrentHashMap<>();

    public static void setInstance(ChiefScanner instance) {
        ScannerManager.instance = instance;
    }

    public static ChiefScanner getInstance() {
        return instance;
    }

    static {
        //set default scanner
        instance = new ProxyVfsScanner();
    }

    public static ChiefScanner getScanner(){
        return getScanner(null);
    }

    public static ChiefScanner getScanner(Class<? extends ChiefScanner> pointType){
        if (pointType == null){
            return instance;
        }

        if (!BeanUtil.isSolidClass(pointType)){
            throw new IllegalStateException("point scanner type is not solid");
        }

        return cache.computeIfAbsent(pointType, t -> {
            return InstanceBeanManager.instance(t, InstanceType.REFLEX_AND_BEAN_FACTORY);
        });
    }

}
