package com.black.core.aop;

import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AopMethodDirectAgent implements ApplicationDriver{

    static AopMethodDirectAgent agent;

    final Map<Class<?>, Map<Method, Boolean>> checkCache = new ConcurrentHashMap<>();

    final Map<Class<?>, MatchMethod> matchMethodMap = new HashMap<>();

    public AopMethodDirectAgent(){}

    public AopMatchTargetClazzAndMethodMutesHandler getHandler(Object bean){
        return getHandler(bean, true);
    }

    public AopMatchTargetClazzAndMethodMutesHandler getHandler(Object bean, boolean useCache) {
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        MatchMethod matchMethod = matchMethodMap.get(primordialClass);
        if (matchMethod == null){
            return new AopMatchTargetClazzAndMethodMutesHandler() {
                @Override
                public boolean matchClazz(Class<?> targetClazz) {
                    return false;
                }

                @Override
                public boolean matchMethod(Class<?> targetClazz, Method method) {
                    return false;
                }
            };
        }else {
            return new AopMatchTargetClazzAndMethodMutesHandler() {
                @Override
                public boolean matchClazz(Class<?> targetClazz) {
                    return true;
                }

                @Override
                public boolean matchMethod(Class<?> targetClazz, Method method) {
                    if (useCache){
                        Map<Method, Boolean> map = checkCache.computeIfAbsent(primordialClass, pc -> new HashMap<>());
                        if (!map.containsKey(method)){
                            boolean b = matchMethod.matchMethod(targetClazz, method);
                            map.put(method, b);
                            return b;
                        }else
                            return map.get(method);
                    }else {
                        return matchMethod.matchMethod(targetClazz, method);
                    }
                }
            };
        }
    }

    public static AopMethodDirectAgent getInstance() {
        if (agent == null){
            FactoryManager.createInstanceFactory();
            InstanceFactory factory = FactoryManager.getInstanceFactory();
            agent = factory.getInstance(AopMethodDirectAgent.class);
        }

        return agent;
    }

    public void register(Object bean, MatchMethod matchMethod){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        matchMethodMap.put(primordialClass, matchMethod);
    }

    @Override
    public void whenApplicationStart(ChiefExpansivelyApplication application) {
        checkCache.clear();
        matchMethodMap.clear();
    }

    public interface MatchMethod{
        boolean matchMethod(Class<?> targetClazz, Method method);
    }

}
