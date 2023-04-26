package com.black.core.bean;

import com.black.nest.Dict;
import com.black.nest.Nest;
import com.black.core.chain.Judge;
import com.black.core.json.Trust;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class TrustBeanCollector implements PostPatternClazzDriver {

    private static final Map<String, Class<?>> trustBeanClassCache = new ConcurrentHashMap<>();

    public static Map<String, Class<?>> getTrustBeanClassCache() {
        return trustBeanClassCache;
    }

    public static final LinkedBlockingQueue<Judge> judgeQueue = new LinkedBlockingQueue<>();

    static {
        registerJudge(clazz -> clazz.isAnnotationPresent(Trust.class));
        registerTrustBean(Dict.class);
        registerTrustBean(Nest.class);
    }

    public static void registerJudge(Judge judge){
        judgeQueue.add(judge);
    }

    public static void clear(){
        trustBeanClassCache.clear();
    }

    public static void registerTrustBean(Class<?> type){
        if (type != null){
            String simpleName = type.getSimpleName();
            trustBeanClassCache.put(simpleName.toLowerCase(), type);
        }
    }

    public static Class<?> getTrustBean(@NonNull String alias){
        String lowerAlias = alias.toLowerCase();
        Class<?> trustBean = trustBeanClassCache.get(lowerAlias);
        Assert.notNull(trustBean, "unregister trust bean:" + alias);
        return trustBean;
    }

    public static boolean existTrustBean(Class<?> type){
        return existTrustBean(type.getSimpleName());
    }

    public static boolean existTrustBean(String alias){
        String lowerAlias = alias.toLowerCase();
        return trustBeanClassCache.containsKey(lowerAlias);
    }

    @Override
    public void postPatternClazz(Class<?> beanClazz, Map<Class<? extends OpenComponent>, Object> springLoadComponent, ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        if (BeanUtil.isSolidClass(beanClazz)){
            for (Judge judge : judgeQueue) {
                if (judge.condition(beanClazz)) {
                    registerTrustBean(beanClazz);
                    return;
                }
            }
        }
    }

}
