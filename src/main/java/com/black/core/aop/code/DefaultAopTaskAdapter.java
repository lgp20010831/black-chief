package com.black.core.aop.code;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultAopTaskAdapter extends AbstractAopTaskQueueAdapter{

    protected ResolverAopHybrid resolverAopHybrid;

    protected PitchClassWithMethodsWrapperFactory wrapperFactory;

    protected Collection<AopTaskManagerHybrid> hybrids;

    protected final Map<PitchClassWithMethodsWrapper, Collection<Method>> hasDoCallBackCache = new ConcurrentHashMap<>();

    public DefaultAopTaskAdapter(){
        super();
        wrapperFactory = new PitchClassWithMethodsWrapperFactory();

    }

    @Override
    protected void handlerHybrids() {
        if (resolverAopHybrid == null){
            resolverAopHybrid = new DefaultResolverAopHybrid();
        }
        if (hybrids == null){
            hybrids = new HashSet<>();
        }
        hybrids.addAll(resolverAopHybrid.handlerAopHybrids(sourceMutes, this));
        for (AopTaskManagerHybrid hybrid : hybrids) {
            hybrid.ifCollectCallBack(this);
        }
    }

    @Override
    protected void handlerAfterIntegrationData(Map<MethodUniqueKey, AopProxyTaskChain> taskChainMap, Map<MethodUniqueKey, HijackObject> hijackObjectMap) {
        GlobalAdviceMethodIntercept intercept = (GlobalAdviceMethodIntercept) getAdvice();
        intercept.registerAllHijack(hijackObjectMap);
        intercept.registerAllTaskChain(taskChainMap);
        intercept.setAopTaskQueueAdapter(this);
    }

    @Override
    protected Set<Class<?>> collectClassMutes() {
        return AopApplicationContext.getSource();
    }

    @Override
    public void registerAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public void successfulMatchClass(Class<?> clazz, AopTaskManagerHybrid hybrid) {
        PitchClassWithMethodsWrapper wrapper = wrapperFactory.createWrapper(clazz, hybrid);
        if (!hasDoCallBackCache.containsKey(wrapper)) {
            hybrid.ifMatchCallBack(wrapper);
            hasDoCallBackCache.put(wrapper, new HashSet<>());
        }
    }

    @Override
    public void successfulMatchMethod(Class<?> clazz, Method method, AopTaskManagerHybrid hybrid) {
        PitchClassWithMethodsWrapper wrapper = wrapperFactory.createWrapper(clazz, method, hybrid);
        Collection<Method> methods = hasDoCallBackCache.get(wrapper);
        if (!methods.contains(method)){
            hybrid.ifMatchCallBack(wrapper);
            methods.add(method);
        }
        factory.registerMapping(clazz, method, hybrid.obtainAopTaskIntercept(), hybrid);
    }

    @Override
    public Collection<AopTaskManagerHybrid> getHybrids() {
        return hybrids;
    }

    @Override
    public HijackObjectFactory obtainFactory() {
        if (factory == null){
            factory = new HijackObjectFactory(this);
        }
        return factory;
    }

    @Override
    public InterceptHijackWrapperFactory obtainWrapperFactory() {
        if (hijackWrapperFactory == null){
            hijackWrapperFactory = new InterceptHijackWrapperFactory();
        }
        return hijackWrapperFactory;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        if (advice == null){
            advice = GlobalAdviceMethodIntercept.getInstance();
        }
        return advice;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }
}
