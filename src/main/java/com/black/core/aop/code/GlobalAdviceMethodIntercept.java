package com.black.core.aop.code;


import com.black.core.tools.BeanUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalAdviceMethodIntercept implements MethodInterceptor {

    private static GlobalAdviceMethodIntercept intercept;

    public synchronized static GlobalAdviceMethodIntercept getInstance() {
        if (intercept == null){
            intercept = new GlobalAdviceMethodIntercept();
        }
        return intercept;
    }

    private final Map<MethodUniqueKey, AopProxyTaskChain> chain = new ConcurrentHashMap<>();

    private final Map<MethodUniqueKey, HijackObject> hijackObjectCache = new ConcurrentHashMap<>();

    private AbstractAopTaskQueueAdapter aopTaskQueueAdapter;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        MethodUniqueKey key = new MethodUniqueKey(method, BeanUtil.getPrimordialClass(invocation.getThis()));
        AopProxyTaskChain taskChain = chain.get(key);
        HijackObject hijackObject = hijackObjectCache.get(key);
        if (taskChain != null && hijackObject != null){
            hijackObject.reset(invocation);
            return taskChain.doInvoke(hijackObject);
        }
        return invocation.proceed();
    }

    public void setAopTaskQueueAdapter(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        this.aopTaskQueueAdapter = aopTaskQueueAdapter;
    }

    public void registerAllHijack(Map<MethodUniqueKey, HijackObject> hijackObjects){
        hijackObjectCache.putAll(hijackObjects);
    }

    public void registerAllTaskChain(Map<MethodUniqueKey, AopProxyTaskChain> chainMap){
        chain.putAll(chainMap);
    }

    public void registerHijack(MethodUniqueKey key, HijackObject object){
        hijackObjectCache.put(key, object);
    }

    public void registerTaskChain(MethodUniqueKey key, AopProxyTaskChain chain){
        this.chain.put(key, chain);
    }
}
