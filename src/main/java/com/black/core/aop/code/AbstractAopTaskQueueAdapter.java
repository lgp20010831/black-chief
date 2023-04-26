package com.black.core.aop.code;

import com.black.core.cache.GlobalAopDispatcherCache;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAopTaskQueueAdapter implements AopTaskChain, GlobalCollectStage,
        GlobalAopMamatchingDispatcher, AopGlobalAdvisor, ApplicationListener<ContextRefreshedEvent> {

    /** 缓存所有记录下的 advice */
    protected final Set<Advice> examinationAdvices = new HashSet<>();

    /** 所有有效的 advice */
    protected final Set<MethodInterceptor> qualifiedAdvices = new HashSet<>();

    /** 当 advice 不属于拦截类型, 则需要指定对应拦截器 */
    protected final Map<Advice, MethodInterceptor> advicePointInterceptor = new HashMap<>();

    protected final Set<Class<?>> sourceMutes;

    protected Pointcut pointcut;

    protected HijackObjectFactory factory;

    protected InterceptHijackWrapperFactory hijackWrapperFactory;

    protected Advice advice;

    public AbstractAopTaskQueueAdapter(){
        GlobalAopDispatcherCache.setAbstractAopTaskQueueAdapter(this);
        factory = new HijackObjectFactory(this);
        registerSelf();

        //收集所有的 class
        sourceMutes = collectQualifiedClassMutes();

        //收集获取所有的子节点
        handlerHybrids();

        obtainPointCut();
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null){
            Map<MethodUniqueKey, AopProxyTaskChain> taskChainMap = factory.integrationTaskChain(false);
            Map<MethodUniqueKey, HijackObject> hijackObjectMap = factory.integrationHijackObject();
            handlerAfterIntegrationData(taskChainMap, hijackObjectMap);
        }
    }

    @Override
    @SuppressWarnings("all")
    public boolean supportsAdvice(Advice advice) {
        examinationAdvices.add(advice);
        return qualifiedAdvices.contains(advice) && !(advice instanceof MethodInterceptor);
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        Advice advice = advisor.getAdvice();
        if (advice instanceof MethodInterceptor){
            return (MethodInterceptor) advice;
        }else {
            return advicePointInterceptor.get(advice);
        }
    }

    protected void registerSelf(){
        AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
        registry.registerAdvisorAdapter(this);
    }

    @Override
    public Set<Class<?>> collectQualifiedClassMutes() {
        if (sourceMutes == null){
            return collectClassMutes();
        }
        return sourceMutes;
    }
    protected abstract void handlerAfterIntegrationData(Map<MethodUniqueKey, AopProxyTaskChain> taskChainMap,
                                                        Map<MethodUniqueKey, HijackObject> hijackObjectMap);

    protected abstract void handlerHybrids();

    protected abstract Set<Class<?>> collectClassMutes();

    public abstract void registerAdvice(Advice advice);

    public void pointInterceptor(Advice advice, MethodInterceptor interceptor){
        advicePointInterceptor.put(advice, interceptor);
    }

    protected void obtainPointCut(){
        pointcut = GlobalAopPointCutHodler.getInstance(this);
    }
}
