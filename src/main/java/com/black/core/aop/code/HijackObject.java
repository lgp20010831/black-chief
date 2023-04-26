package com.black.core.aop.code;

import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
//aop 劫持的对象
public final class HijackObject {
    //任务队列
    private final AopProxyTaskChain chain;
    //队列指针
    private final ThreadLocal<AtomicInteger> ayncIndex = new ThreadLocal<>();

    //每个线程当前正在执行的节点
    private final ThreadLocal<InterceptHijackWrapper> pointHijackWrapper = new ThreadLocal<>();

    //每个线程维持的参数列表
    private final ThreadLocal<Object[]> argsLocal = new ThreadLocal<>();
    private final Method method;
    private final Class<?> clazz;
    private final ThreadLocal<MethodInvocation> methodInvocation = new ThreadLocal<>();
    public HijackObject(AopProxyTaskChain chain, Method method, Class<?> clazz) {
        this.chain = chain;
        this.method = method;
        this.clazz = clazz;
    }

    public InterceptHijackWrapper getPointHijack(){
        return pointHijackWrapper.get();
    }


    public MethodInvocation getInvocation(){
        return methodInvocation.get();
    }

    public void reset(MethodInvocation invocation){
        methodInvocation.remove();
        methodInvocation.set(invocation);
        argsLocal.remove();
        argsLocal.set(invocation.getArguments());
        AtomicInteger integer = ayncIndex.get();
        if (integer == null){
            integer = new AtomicInteger(0);
            ayncIndex.set(integer);
        }
        integer.set(0);
    }

    public Method getMethod() {
        return method;
    }

   public Class<?> getClazz(){
        return clazz;
   }

    public Object[] getArgs(){
        return argsLocal.get();
    }

    public void resetArgs(Object[] args){
        argsLocal.remove();
        argsLocal.set(args);
    }

    //放行
    public Object doRelease(Object[] args) throws Throwable {
        AtomicInteger integer = ayncIndex.get();
        integer.incrementAndGet();
        int index = integer.get();
        if (index > chain.size() - 1){
            if (log.isErrorEnabled()) {
                log.error("error point !!!");
            }
            return chain.get(chain.size() - 1).getIntercepet().processor(this);
        }
        resetArgs(args);
        InterceptHijackWrapper hijackWrapper = chain.get(index);
        AopTaskIntercepet intercepet = hijackWrapper.getIntercepet();
        pointHijackWrapper.remove();
        pointHijackWrapper.set(hijackWrapper);
        return intercepet.processor(this);
    }
}
