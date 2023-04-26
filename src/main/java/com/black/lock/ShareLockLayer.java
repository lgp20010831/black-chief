package com.black.lock;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.chain.GroupKeys;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.util.AnnotationUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Log4j2
public class ShareLockLayer implements AgentLayer {

    private final Map<GroupKeys, LockConfiguration> configurationCache = new ConcurrentHashMap<>();

    private final Map<GroupKeys, Semaphore> semaphoreCache = new ConcurrentHashMap<>();

    public ShareLockLayer(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        final Class<?> agentClazz = layer.getAgentClazz();
        final Method proxyMethod = layer.getProxyMethod();
        Object[] args = layer.getArgs();
        LockConfiguration configuration = getConfiguration(agentClazz, proxyMethod);
        Semaphore semaphore = null;
        if (configuration != null){
            semaphore = getSemaphore(agentClazz, proxyMethod, configuration);
            try {
                semaphore.acquire();
            }catch (InterruptedException ie){
                System.out.println(AnsiOutput.toString(AnsiColor.RED, "thread interrupted: [" + Thread.currentThread().getName() + "]"));
                semaphore = null;
            }
            processorLockArg(args, semaphore, proxyMethod);
            System.out.println(AnsiOutput.toString(AnsiColor.RED, "lock Thread: [" + Thread.currentThread().getName() + "]"));
        }
        try {

            return layer.doFlow(args);
        }finally {
            if (semaphore != null){
                semaphore.release();
                System.out.println(AnsiOutput.toString(AnsiColor.GREEN, "unlock Thread: [" + Thread.currentThread().getName() + "]"));
            }
        }
    }

    private void processorLockArg(Object[] args, Semaphore semaphore, Method proxyMethod){
        MethodWrapper mw = MethodWrapper.get(proxyMethod);
        ParameterWrapper pw = mw.getSingleParameterByAnnotation(ShareLock.class);
        if (pw != null && pw.getType().equals(Semaphore.class)){
            args[pw.getIndex()] = semaphore;
        }
    }

    private LockConfiguration getConfiguration(Class<?> agentClazz, Method method){
        ShareLock annotation = method.getAnnotation(ShareLock.class);
        if (annotation == null){
            annotation = agentClazz.getAnnotation(ShareLock.class);
        }
        if (annotation != null){
            GroupKeys groupKeys = new GroupKeys(agentClazz, method);
            final ShareLock fa = annotation;
            return configurationCache.computeIfAbsent(groupKeys, gk -> {
                LockConfiguration configuration = new LockConfiguration();
                return AnnotationUtils.loadAttribute(fa, configuration);
            });
        }
        return null;
    }

    private Semaphore getSemaphore(Class<?> agentClazz, Method method, LockConfiguration configuration){
        GroupKeys groupKeys = new GroupKeys(agentClazz, method);
        return semaphoreCache.computeIfAbsent(groupKeys, gk -> {
            return new Semaphore(configuration.getLimit(), configuration.isFair());
        });
    }

}
