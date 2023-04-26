package com.black.core.event;

import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.code.*;
import com.black.core.chain.*;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflexHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//注解类型的事件分发器
//需求需要发布事件的时候, 不需要在实现什么
@Log4j2
@ChainClient
//设配器
@Adaptation(EventAdapter.class)
//构造前提
@AopHybrid(EventPremise.class)
public class AnnotationEventAutoDispenser implements AopTaskManagerHybrid, CollectedCilent {

    private final Set<Class<?>> publisherMutes = new HashSet<>();
    private final Set<String> entries = new HashSet<>();
    public static final String EVENT_ALIAS = "AnnotationEventAutoDispenser";
    private final EntryParser entryParser = new EntryParser();
    private EventInterceptor interceptor;
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        return new AopMatchTargetClazzAndMethodMutesHandler() {
            @Override
            public boolean matchClazz(Class<?> targetClazz) {
                Class<?> primordialClass = BeanUtil.getPrimordialClass(targetClazz);
                EventPublisher publisher = AnnotationUtils.getAnnotation(primordialClass, EventPublisher.class);
                if (publisher != null){
                    publisherMutes.add(primordialClass);
                    return true;
                }

                return false;
            }

            @Override
            public boolean matchMethod(Class<?> targetClazz, Method method) {
                if (publisherMutes.contains(targetClazz)){
                    WriteEvent writeEvent = AnnotationUtils.getAnnotation(method, WriteEvent.class);
                    if (writeEvent != null){
                        String[] value = writeEvent.value();
                        for (String s : value) {
                            if (s.contains("*")){
                                throw new RuntimeException("事件发送标志不能带有通配符:" + s);
                            }
                            entries.add(s);
                        }
                        return true;
                    }
                    return false;
                }
                return false;
            }
        };
    }

    @Override
    public void ifCollectCallBack(GlobalAopMamatchingDispatcher dispatcher) {

    }

    @Override
    public void ifMatchCallBack(PitchClassWithMethodsWrapper wrapper) {

    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        if (interceptor == null){
            interceptor = new EventInterceptor();
        }
        return interceptor;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry conditionEntry = register.begin();
        conditionEntry.setAlias(EVENT_ALIAS);
        conditionEntry.needOrder(false);
        conditionEntry.condition(e -> AnnotationUtils.getAnnotation(e, Eventlistener.class) != null);

    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals(EVENT_ALIAS)) {
            Collection<Object> source = resultBody.getCollectSource();
            EventInterceptor intercepet = (EventInterceptor) obtainAopTaskIntercept();
            for (Object obj : source) {
                for (Method method : ReflexHandler.getAccessibleMethods(obj)) {
                    ResolverThrowable resolverThrowable;
                    ReadEvent readEvent = AnnotationUtils.getAnnotation(method, ReadEvent.class);
                    if (readEvent != null){

                        //构造一个wrapper
                        EventOperatorWrapper wrapper = new EventOperatorWrapper(method, obj, readEvent);
                        String[] es = readEvent.value();
                        for (String e : es) {
                            String[] resolver = entryParser.resolver(entries, e);
                            for (String r : resolver) {
                                intercepet.put(r, wrapper);
                            }
                        }
                    }else if ((resolverThrowable = AnnotationUtils.getAnnotation(method, ResolverThrowable.class)) != null){
                        EventErrorWrapper errorWrapper = new EventErrorWrapper(method, obj, resolverThrowable);
                        String[] value = resolverThrowable.value();
                        for (String v : value) {
                            for (String s : entryParser.resolver(entries, v)) {
                                intercepet.put(s, errorWrapper);
                            }
                        }
                    }

                    //loop method
                }
            }
            intercepet.end(entries);
        }
    }
}
