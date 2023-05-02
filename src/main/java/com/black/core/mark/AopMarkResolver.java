package com.black.core.mark;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.*;
import com.black.core.chain.*;
import com.black.core.mark.annotation.EnabledGlobalMarkHandler;
import com.black.core.mark.annotation.GlobalMarks;
import com.black.core.mark.annotation.MarkResolver;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.tools.BeanUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AopHybrid(AopMarkResolver.class)
@HybridSort(4567)  @Log4j2
@ChainClient(AopMarkResolver.class)
public class AopMarkResolver implements Premise, AopTaskManagerHybrid, AopTaskIntercepet,
        ChainPremise, CollectedCilent {

    @Getter
    int size = 0;


    final Map<String, Collection<MarksHandler>> handlerMap = new ConcurrentHashMap<>();

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            boolean pro = AnnotationUtils.getAnnotation(method, GlobalMarks.class) != null;
            if (pro) size ++;
            return pro;
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        final Method method = hijack.getMethod();
        Object[] args = hijack.getArgs();
        MethodWrapper wrapper = MethodWrapper.get(method);
        //拿到注解
        GlobalMarks annotation = AnnotationUtils.getAnnotation(method, GlobalMarks.class);
        if (annotation == null) return hijack.doRelease(hijack.getArgs());

        String[] arms = annotation.value();
        //处理参数
        for (String arm : arms) {
            Collection<MarksHandler> handlers = handlerMap.get(arm);
            if (handlers != null){
                for (MarksHandler handler : handlers) {
                    args = handler.parseParams(wrapper, args);
                }
            }
        }

        //判断是否对方法进行拦截
        boolean intercept = false;
        arminter: for (String arm : arms) {
            Collection<MarksHandler> handlers = handlerMap.get(arm);
            if (handlers != null){
                for (MarksHandler handler : handlers) {
                    intercept = handler.intercept(wrapper, args);
                    if (intercept){
                        break arminter;
                    }
                }
            }
        }

        //最终结果
        Object result = null;
        if (intercept){
            //拦截成功执行回调
            for (String arm : arms) {
                Collection<MarksHandler> handlers = handlerMap.get(arm);
                if (handlers != null){
                    for (MarksHandler handler : handlers) {
                        result = handler.interceptCallBack(wrapper, args, result);
                    }
                }
            }
        }else {
            try {
                //拦截失败, 执行前置处理
                for (String arm : arms) {
                    Collection<MarksHandler> handlers = handlerMap.get(arm);
                    if (handlers != null){
                        for (MarksHandler handler : handlers) {
                            args = handler.beforeRelease(wrapper, args);
                        }
                    }
                }
                //执行方法
                result = hijack.doRelease(hijack.getArgs());

                //后置处理
                for (String arm : arms) {
                    Collection<MarksHandler> handlers = handlerMap.get(arm);
                    if (handlers != null){
                        for (MarksHandler handler : handlers) {
                            result = handler.afterRelease(wrapper, result);
                        }
                    }
                }
            }catch (Throwable ex){
                Throwable proex = ex;
                //是否捕获住异常
                boolean capture = false;
                for (String arm : arms) {
                    Collection<MarksHandler> handlers = handlerMap.get(arm);
                    if (handlers != null){
                        for (MarksHandler handler : handlers) {
                            try {
                                result = handler.processorThrowable(wrapper, proex, result);
                                //如果有处理器没有抛出异常, 则为捕获成功
                                capture = true;
                            }catch (Throwable markex){
                                proex = markex;
                            }
                        }
                    }
                }
                if (!capture){
                    //如果没有捕获抛出异常
                    throw proex;
                }
            }
        }
        return result;
    }


    @Override
    public boolean condition(AbstractAopTaskQueueAdapter aopTaskQueueAdapter) {
        return ChiefApplicationRunner.isPertain(EnabledGlobalMarkHandler.class);
    }


    @Override
    public boolean premise() {
        return condition(null);
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        log.info("mark methods: [{}]", size);
        register.begin("mark", chu -> {
            return MarksHandler.class.isAssignableFrom(chu) &&
                    BeanUtil.isSolidClass(chu) && AnnotationUtils.getAnnotation(chu, MarkResolver.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("mark".equals(resultBody.getAlias())) {
            Collection<Object> objects = resultBody.getCollectSource();
            log.info("mark handlers: [{}]", objects.size());
            for (Object obj : objects) {
                MarkResolver annotation = AnnotationUtils.getAnnotation(BeanUtil.getPrimordialClass(obj), MarkResolver.class);
                if (annotation == null || !(obj instanceof MarksHandler)) continue;
                for (String arm : annotation.value()) {
                    Collection<MarksHandler> marksHandlers = handlerMap.computeIfAbsent(arm, ar -> new HashSet<>());
                    marksHandlers.add((MarksHandler) obj);
                }
            }
        }
    }
}
