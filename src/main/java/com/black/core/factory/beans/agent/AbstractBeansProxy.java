package com.black.core.factory.beans.agent;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.annotation.Lock;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.factory.beans.lock.KnitLock;
import com.black.core.factory.beans.lock.LockConfig;
import com.black.core.factory.beans.lock.ReentrantKnitLock;
import com.black.core.factory.beans.lock.SemaphoreLock;
import com.black.core.query.MethodWrapper;
import com.black.core.util.AnnotationUtils;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-05-23 16:42
 */
@SuppressWarnings("all")
@Log4j2
public abstract class AbstractBeansProxy {


    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

    private final Map<Object, KnitLock> lockMap = new ConcurrentHashMap<>();

    private final Map<Method, LockConfig> lockConfigMap = new ConcurrentHashMap<>();

    private final ThreadLocal<KnitLock> lockLocal = new ThreadLocal<>();

    protected final BeanFactory factory;

    protected final BeanDefinitional<?> definitional;

    protected AbstractBeansProxy(BeanFactory factory, BeanDefinitional<?> definitional) {
        this.factory = factory;
        this.definitional = definitional;
    }
    protected void tryLock(MethodWrapper methodWrapper, Object[] args){
        if (!methodWrapper.hasAnnotation(Lock.class)) {
            return;
        }

        KnitLock parentLock = lockLocal.get();
        if (parentLock != null){
            if (log.isDebugEnabled()) {
                log.debug("currently in a locked state");
            }
            return;
        }

        LockConfig config = lockConfigMap.computeIfAbsent(methodWrapper.getMethod(), m -> {
            Lock annotation = methodWrapper.getAnnotation(Lock.class);
            return AnnotationUtils.loadAttribute(annotation, new LockConfig());
        });
        Object groupKey;
        int group = config.getGroup();
        if (group < 0){
            groupKey = DEFAULT_GROUP;
        }else {
            if (args.length <= group){
                throw new IllegalStateException("obtaining a group will throw " +
                        "a subscript error, index: " + group);
            }

            groupKey = args[group];
            if (groupKey == null){
                throw new IllegalStateException("the parameter cannot be empty " +
                        "for external grouping");
            }
        }
        KnitLock knitLock = lockMap.computeIfAbsent(groupKey, gk -> {
            return createLock(config);
        });
        lockLocal.set(knitLock);
        knitLock.lock();
    }

    protected KnitLock createLock(LockConfig config){
        int share = config.getShare();
        boolean fair = config.isFair();
        if (share > 1){
            return new SemaphoreLock(share, fair);
        }else {
            return new ReentrantKnitLock(fair);
        }
    }

    protected void tryUnlock(){
        KnitLock knitLock = lockLocal.get();
        if (knitLock != null){
            try {
                knitLock.unlock();
            }finally {
                lockLocal.remove();
            }
        }
    }

    protected boolean isQualified(MethodWrapper methodWrapper){
        return definitional.isQualified(methodWrapper);
    }

    protected Object[] checkArgs(Object[] args){
        return args == null ? new Object[0] : args;
    }

    protected void checkNotNullArgs(MethodWrapper methodWrapper, Object[] args){
        ParameterWrapper[] parameterArray = methodWrapper.getParameterArray();
        for (ParameterWrapper parameterWrapper : parameterArray) {
            Object arg = args[parameterWrapper.getIndex()];
            if (parameterWrapper.hasAnnotation(NotNull.class)){
                if (arg == null){
                    throw new IllegalArgumentException("parameter should not be empty: " + parameterWrapper.getName());
                }
            }
        }
    }

    protected Object[] prepareArgs(MethodWrapper methodWrapper, Object[] args, Object bean){
        return factory.prepareMethodParams(args, bean, methodWrapper);
    }

    protected Object resolveResult(MethodWrapper methodWrapper, Object result, Object bean){
        return factory.afterInvokeMethod(bean, result, methodWrapper);
    }
}
