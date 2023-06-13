package com.black.core.aop.weaving;

import com.black.core.aop.code.*;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class AopWeavingRegister {

    private final static IoLog log = LogFactory.getArrayLog();

    public static boolean print = false;

    public static void setPrint(boolean print) {
        AopWeavingRegister.print = print;
    }

    private static AopWeavingRegister weavingRegister;

    public AopWeavingRegister() {
        hijackObjectFactory = new HijackObjectFactory();
    }

    public synchronized static AopWeavingRegister getInstance() {
        if (weavingRegister == null){
            weavingRegister = new AopWeavingRegister();
        }
        return weavingRegister;
    }

    private final HijackObjectFactory hijackObjectFactory;

    public HijackObjectFactory getHijackObjectFactory() {
        return hijackObjectFactory;
    }

    public void register(@NonNull Class<?> clazz){
        log.info("[AopWeavingRegister] weaving component: {}", clazz.getSimpleName());
        ClassWrapper<?> cw = ClassWrapper.get(clazz);
        Collection<MethodWrapper> methodWrappers = cw.getMethods();
        Map<AopTaskManagerHybrid, AopMatchTargetClazzAndMethodMutesHandler> matchCache = GlobalAopPointCutHodler.getMatchCache();
        for (AopTaskManagerHybrid hybrid : matchCache.keySet()) {
            AopMatchTargetClazzAndMethodMutesHandler handler = matchCache.get(hybrid);
            if (!handler.matchClazz(clazz)) {
                continue;
            }
            PitchClassWithMethodsWrapper pitchClassWithMethodsWrapper = new PitchClassWithMethodsWrapper(clazz);
            AopTaskIntercepet intercepet = hybrid.obtainAopTaskIntercept();
            for (MethodWrapper mw : methodWrappers) {
                Method method = mw.get();
                if (handler.matchMethod(clazz, method)) {
                    if (print){
                        log.trace("[AopWeavingRegister] clazz: {} with method: {} be woven into by handler --> {}",
                                clazz.getSimpleName(), method.getName(), handler);

                    }
                    pitchClassWithMethodsWrapper.addMethod(method);
                    hijackObjectFactory.registerMapping(clazz, method, intercepet, hybrid);
                }
            }
            hybrid.ifMatchCallBack(pitchClassWithMethodsWrapper);
        }

    }

    public void flush(){
        log.trace("[AopWeavingRegister] flush...");
        Map<MethodUniqueKey, AopProxyTaskChain> chains = hijackObjectFactory.integrationTaskChain(false);
        Map<MethodUniqueKey, HijackObject> hijackObjects = hijackObjectFactory.integrationHijackObject();
        GlobalAdviceMethodIntercept intercept = GlobalAdviceMethodIntercept.getInstance();
        intercept.registerAllTaskChain(chains);
        intercept.registerAllHijack(hijackObjects);
        hijackObjectFactory.clear();
    }
}
