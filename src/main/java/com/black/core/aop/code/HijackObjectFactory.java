package com.black.core.aop.code;

import com.black.core.builder.SortUtil;
import com.black.core.tools.BeanUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HijackObjectFactory {

    private final Map<AopTaskIntercepet, InterceptInitialInformation> informations = new ConcurrentHashMap<>();

    private final Map<MethodUniqueKey, AopProxyTaskChain> taskChainCache = new ConcurrentHashMap<>();

    private final Map<MethodUniqueKey, HijackObject> hijackCache = new ConcurrentHashMap<>();

    private final InterceptHijackWrapperFactory hijackWrapperFactory;

    private JoinInterceptToTaskChain joinInterceptToTaskChain;

    public HijackObjectFactory(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        hijackWrapperFactory = aopTaskQueueAdapter.obtainWrapperFactory();
        joinInterceptToTaskChain = new DefaultJoinHandler(hijackWrapperFactory);
    }

    public HijackObjectFactory(){
        hijackWrapperFactory = new InterceptHijackWrapperFactory();
        joinInterceptToTaskChain = new DefaultJoinHandler(hijackWrapperFactory);
    }

    public void setJoinInterceptToTaskChain(JoinInterceptToTaskChain joinInterceptToTaskChain) {
        this.joinInterceptToTaskChain = joinInterceptToTaskChain;
    }

    public void registerMapping(Class<?> clazz, Method method, AopTaskIntercepet intercepet){
        InterceptInitialInformation information = informations
                .computeIfAbsent(intercepet, InterceptInitialInformation::new);

        information.registerClass(clazz);
        information.registerMethod(clazz, method);
    }

    //整合所有的 信息
    public Map<MethodUniqueKey, AopProxyTaskChain> integrationTaskChain(boolean force){
        if (taskChainCache.isEmpty() || force){
            ArrayList<InterceptInitialInformation> beforeSort = new ArrayList<>(informations.values());
            ArrayList<InterceptInitialInformation> afterSort = SortUtil.sort("order", beforeSort);
            for (InterceptInitialInformation information : afterSort) {
                Map<Class<?>, Collection<Method>> mappingCondition = information.getMappingCondition();
                mappingCondition.forEach((cla, methodSet) ->{
                    for (Method method : methodSet) {
                        MethodUniqueKey uniqueKey = new MethodUniqueKey(method, BeanUtil.getPrimordialClass(cla));
                        AopProxyTaskChain taskChain = taskChainCache.computeIfAbsent(uniqueKey,
                                m -> new AopProxyTaskChain(hijackWrapperFactory.createWrapper(createLastIntercept())));
                        joinInterceptToTaskChain.addChain(taskChain, information);
                    }
                });
            }
        }
        return taskChainCache;
    }

    public Map<MethodUniqueKey, HijackObject> integrationHijackObject(){
        taskChainCache.forEach((key, chain) ->{
            if (!hijackCache.containsKey(key)){
                hijackCache.put(key, new HijackObject(chain, key.getMethod(), key.getTargetClazz()));
            }
        });
        return hijackCache;
    }

    public void clear(){
        taskChainCache.clear();
        hijackCache.clear();
        informations.clear();
    }

    protected AopTaskIntercepet createLastIntercept(){
        return new LastNode();
    }

    public static class LastNode implements AopTaskIntercepet{

        @Override
        public Object processor(HijackObject hijack) throws Throwable {
            return hijack.getInvocation().proceed();
        }
    }

}
