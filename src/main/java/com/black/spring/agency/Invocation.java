package com.black.spring.agency;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.bin.ProxyTemplate;
import com.black.function.Function;
import com.black.pattern.MethodInvoker;
import com.black.core.aop.code.HijackObject;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shkstart
 * @create 2023-04-17 14:12
 */
@Getter @Log4j2
public class Invocation {

    private final LinkedBlockingQueue<MethodInvoker> methodInvokerLinkedBlockingQueue = new LinkedBlockingQueue<>();

    private final List<MethodInvoker> invokeCache = new ArrayList<>();

    private Object[] args;

    private final Object bean;

    private final Method method;

    private final Function<Object[], Object> invokeFun;

    private MethodReflectionIntoTheParameterProcessor parameterProcessor;

    public Invocation(List<MethodInvoker> methodInvokers, ProxyTemplate template, Object[] args) {
        this(methodInvokers, args, template.getBean(), template.getMethod(),
                template::invokeOriginal);
    }

    public Invocation(List<MethodInvoker> methodInvokers,
                      Object[] args,
                      Object bean,
                      Method method,
                      Function<Object[], Object> invokeFun){
        methodInvokerLinkedBlockingQueue.addAll(methodInvokers);
        invokeCache.addAll(methodInvokers);
        this.args = args;
        this.method = method;
        this.invokeFun = invokeFun;
        this.bean = bean;
    }

    public Invocation(List<MethodInvoker> methodInvokers, HijackObject hijack){
        this(methodInvokers, hijack.getArgs(), hijack.getInvocation().getThis(),
                hijack.getMethod(), hijack::doRelease);
    }

    public Object getThis(){
        return bean;
    }


    public void setParameterProcessor(MethodReflectionIntoTheParameterProcessor parameterProcessor) {
        this.parameterProcessor = parameterProcessor;
    }

    public Class<?> getType(){
        return BeanUtil.getPrimordialClass(getThis());
    }

    public Method getMethod(){
        return method;
    }


    public Object invoke() throws Throwable {
        return invoke(getArgs());
    }

    public Object invoke(Object[] args) throws Throwable {
        this.args = args;
        MethodInvoker next = methodInvokerLinkedBlockingQueue.poll();
        if (next == null){
            return invokeFun.apply(args);
        }else {
            MethodWrapper methodWrapper = next.getMw();
            MethodWrapper originMethodWrapper = MethodWrapper.get(getMethod());
            Object[] tempArgs = args;
            if (parameterProcessor != null){
                tempArgs = parameterProcessor.parse(methodWrapper, originMethodWrapper, args, this);
                log.info("proxy Jump to {}", next);
            }
            return next.invoke(tempArgs);
        }
    }

    public void reset(){
        methodInvokerLinkedBlockingQueue.clear();
        methodInvokerLinkedBlockingQueue.addAll(invokeCache);
    }

}
