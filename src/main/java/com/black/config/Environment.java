package com.black.config;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.config.inferrer.ClassAttributeInferrer;
import com.black.config.intercept.AttributeSetIntercept;
import com.black.config.supportor.AttributeInjectorSupportor;
import com.black.config.throwable.BeanAttributeThrower;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.tools.BeanUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.LinkedBlockingQueue;

@Getter @Setter
public class Environment {

    private static Environment environment;

    public synchronized static Environment getInstance() {
        if (environment == null){
            environment = new Environment();
        }
        return environment;
    }

    private Environment(){
        init();
    }

    private synchronized void init(){
        ChiefScanner scanner = ScannerManager.getScanner();
        for (Class<?> clazz : scanner.load("com.black.config.inferrer")) {
            if (BeanUtil.isSolidClass(clazz) && ClassAttributeInferrer.class.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                inferrers.add((ClassAttributeInferrer) instance);
            }
        }

        for (Class<?> clazz : scanner.load("com.black.config.supportor")) {
            if (BeanUtil.isSolidClass(clazz) && AttributeInjectorSupportor.class.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                supportors.add((AttributeInjectorSupportor) instance);
            }
        }

        for (Class<?> clazz : scanner.load("com.black.config.intercept")) {
            if (BeanUtil.isSolidClass(clazz) && AttributeSetIntercept.class.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                intercepts.add((AttributeSetIntercept) instance);
            }
        }

        for (Class<?> clazz : scanner.load("com.black.config.throwable")) {
            if (BeanUtil.isSolidClass(clazz) && BeanAttributeThrower.class.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                attributeThrowers.add((BeanAttributeThrower) instance);
            }
        }
    }

    private IoLog log = LogFactory.getArrayLog();

    private final LinkedBlockingQueue<ClassAttributeInferrer> inferrers = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<AttributeInjectorSupportor> supportors = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<AttributeSetIntercept> intercepts = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<BeanAttributeThrower> attributeThrowers = new LinkedBlockingQueue<>();

}
