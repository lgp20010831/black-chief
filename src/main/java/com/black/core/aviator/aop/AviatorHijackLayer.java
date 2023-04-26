package com.black.core.aviator.aop;

import com.black.aviator.AviatorAgentLayer;
import com.black.aviator.AviatorContext;
import com.black.aviator.AviatorsException;
import com.black.aviator.ObjectEnv;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.query.ClassWrapper;
import com.black.core.spring.factory.HijackAgentObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AviatorHijackLayer extends AviatorAgentLayer implements AopTaskIntercepet {

    private final Map<Class<?>, ObjectEnv> envMap = new ConcurrentHashMap<>();

    private ThreadLocal<ObjectEnv> currentEnv = new ThreadLocal<>();

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Class<?> clazz = hijack.getClazz();
        Object proxy = hijack.getInvocation().getThis();
        ObjectEnv objectEnv = envMap.computeIfAbsent(clazz, type -> {
            ObjectEnv env = new ObjectEnv();
            ClassWrapper<?> cw = ClassWrapper.get(clazz);
            env.parseFieldParams(proxy, cw);
            return env;
        });
        currentEnv.set(objectEnv);
        try {
            System.out.println("[AviatorHijackLayer] ==> enter the aviator control range");
            return proxy(HijackAgentObject.of(hijack));
        }finally {
            currentEnv.remove();
        }
    }

    @Override
    public Map<String, Object> getBaseEnv() {
        Map<String, Object> env = new HashMap<>();
        ObjectEnv objectEnv = currentEnv.get();
        if (objectEnv == null){
            throw new AviatorsException("无法获取当前线程下环境变量");
        }
        synchronized (AviatorContext.initialEnvNames){
            for (String initialEnvName : AviatorContext.initialEnvNames) {
                env.put(initialEnvName, objectEnv.getCreateParam());
            }
        }

        synchronized (AviatorContext.fieldEnvNames){
            for (String fieldEnvName : AviatorContext.fieldEnvNames) {
                env.put(fieldEnvName, objectEnv.getFieldParam());
            }
        }
        return env;
    }
}
