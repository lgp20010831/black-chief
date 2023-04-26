package com.black.core.aop.code;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalAopPointCutHodler {

    private static GlobalAopCollectPointCut globalAopCollectPointCut;

    private static final Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> matchCache = new ConcurrentHashMap<>();

    public static GlobalAopCollectPointCut getInstance(AbstractAopTaskQueueAdapter aopTaskQueueAdapter){
        if (globalAopCollectPointCut == null){
            Collection<AopTaskManagerHybrid> hybrids = aopTaskQueueAdapter.getHybrids();
            final Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> hybridMap = new HashMap<>(hybrids.size());
            for (AopTaskManagerHybrid hybrid : hybrids) {
                hybridMap.put(hybrid, hybrid.obtainMatcher());
            }
            matchCache.putAll(hybridMap);
            globalAopCollectPointCut = new GlobalAopCollectPointCut(hybridMap, aopTaskQueueAdapter);
        }
        return globalAopCollectPointCut;
    }

    public static Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> getMatchCache() {
        return matchCache;
    }
}
