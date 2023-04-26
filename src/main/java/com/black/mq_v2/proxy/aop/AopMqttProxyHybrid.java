package com.black.mq_v2.proxy.aop;

import com.black.callback.CallBackRegister;
import com.black.holder.SpringHodler;
import com.black.mq_v2.MQTTException;
import com.black.mq_v2.MqttUtils;
import com.black.mq_v2.annotation.CallBackOnFair;
import com.black.mq_v2.annotation.CallBackOnSuccess;
import com.black.mq_v2.annotation.MqttArrived;
import com.black.mq_v2.annotation.MqttPush;
import com.black.mq_v2.definition.MqttContext;
import com.black.mq_v2.proxy.MessageArrivedMethodRegister;
import com.black.mq_v2.proxy.MessageSendProxyRegister;
import com.black.mq_v2.proxy.RegisterHolder;
import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.code.HijackObject;
import com.black.core.log.IoLog;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Assert;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@AopHybrid(AopMqttPremise.class) @HybridSort(3458)
public class AopMqttProxyHybrid implements AopTaskManagerHybrid, AopTaskIntercepet {

    private final Map<Class<?>, Set<Method>> arrivdCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Set<Method>> successCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Set<Method>> fairCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Set<Method>> sendCache = new ConcurrentHashMap<>();

    public AopMqttProxyHybrid(){
        CallBackRegister.addTask(this::process);
    }

    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {

              if (method.isAnnotationPresent(MqttArrived.class)){
                  Set<Method> methods = arrivdCache.computeIfAbsent(targetClazz, tc -> new HashSet<>());
                  methods.add(method);
              }

              if (method.isAnnotationPresent(CallBackOnSuccess.class)){
                  Set<Method> methods = successCache.computeIfAbsent(targetClazz, tc -> new HashSet<>());
                  methods.add(method);
              }

            if (method.isAnnotationPresent(CallBackOnFair.class)){
                Set<Method> methods = fairCache.computeIfAbsent(targetClazz, tc -> new HashSet<>());
                methods.add(method);
            }

            boolean proxy = false;
            if (method.isAnnotationPresent(MqttPush.class)){
                proxy = true;
                Set<Method> methods = sendCache.computeIfAbsent(targetClazz, tc -> new HashSet<>());
                methods.add(method);
            }

            return proxy;
        });
        return agent.getHandler(this);
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Class<?> clazz = hijack.getClazz();
        Method method = hijack.getMethod();
        MethodWrapper mw = MethodWrapper.get(method);
        String[] mqttNames = MqttUtils.getMqttName(clazz);
        Object result = hijack.doRelease(hijack.getArgs());
        Collection<RegisterHolder> hodlers = getNoRepeatHodlers(mqttNames);
        for (RegisterHolder hodler : hodlers) {
            MqttContext context = hodler.getContext();
            IoLog ioLog = context.getLog();
            MessageSendProxyRegister register = hodler.getSendProxyRegister();
            try {
                register.parseAndSend(mw, hijack.getInvocation().getThis(), result);
            }catch (Throwable e){
                ioLog.debug("[{]] -- An exception occurred when the aop agent sent a message -- {} -- {}",
                        context.getName(), mw.getName(), ServiceUtils.getThrowableMessage(e));
            }
        }
        return result;
    }

    protected Collection<RegisterHolder> getNoRepeatHodlers(String[] names){
        Set<RegisterHolder> holders = new HashSet<>();
        for (String name : names) {
            holders.addAll(getHolders(name));
        }
        return holders;
    }

    protected Collection<RegisterHolder> getHolders(String name){
        MqttContextRegister register = MqttContextRegister.getInstance();
        Collection<RegisterHolder> contexts = register.getContext(name);
        if (contexts.isEmpty()){
            throw new IllegalStateException("can not find mqtt context: " + name);
        }
        return contexts;
    }

    protected Object getBean(Class<?> clazz){
        BeanFactory beanFactory = SpringHodler.getBeanFactory();
        Assert.notNull(beanFactory, "not find bean factory");
        try {
            return beanFactory.getBean(clazz);
        }catch (Throwable e){
            log.info("Unable to get proxy object from factory");
            throw new MQTTException(e);
        }
    }

    public void process(){
        for (Class<?> clazz : arrivdCache.keySet()) {
            Object bean = getBean(clazz);
            String[] mqttName = MqttUtils.getMqttName(clazz);
            for (String name : mqttName) {
                Collection<RegisterHolder> holders = getHolders(name);
                for (RegisterHolder holder : holders) {
                    Set<Method> methods = arrivdCache.get(clazz);
                    for (Method method : methods) {
                        MethodWrapper methodWrapper = MethodWrapper.get(method);
                        MessageArrivedMethodRegister register = holder.getArrivedMethodRegister();
                        register.registerMethodObject(methodWrapper, bean);
                    }
                }
            }
        }

        for (Class<?> clazz : sendCache.keySet()) {
            Object bean = getBean(clazz);
            String[] mqttName = MqttUtils.getMqttName(clazz);
            for (String name : mqttName) {
                Collection<RegisterHolder> holders = getHolders(name);
                for (RegisterHolder holder : holders) {
                    Set<Method> methods = sendCache.get(clazz);
                    for (Method method : methods) {
                        MethodWrapper methodWrapper = MethodWrapper.get(method);
                        MessageSendProxyRegister register = holder.getSendProxyRegister();
                        register.registerMethodObject(methodWrapper, bean);
                    }
                }
            }
        }

        for (Class<?> clazz : successCache.keySet()) {
            Object bean = getBean(clazz);
            String[] mqttName = MqttUtils.getMqttName(clazz);
            for (String name : mqttName) {
                Collection<RegisterHolder> holders = getHolders(name);
                for (RegisterHolder holder : holders) {
                    Set<Method> methods = successCache.get(clazz);
                    for (Method method : methods) {
                        MethodWrapper methodWrapper = MethodWrapper.get(method);
                        MessageSendProxyRegister register = holder.getSendProxyRegister();
                        register.registerSuccessMethodObject(methodWrapper, bean);
                    }
                }
            }
        }


        for (Class<?> clazz : fairCache.keySet()) {
            Object bean = getBean(clazz);
            String[] mqttName = MqttUtils.getMqttName(clazz);
            for (String name : mqttName) {
                Collection<RegisterHolder> holders = getHolders(name);
                for (RegisterHolder holder : holders) {
                    Set<Method> methods = fairCache.get(clazz);
                    for (Method method : methods) {
                        MethodWrapper methodWrapper = MethodWrapper.get(method);
                        MessageSendProxyRegister register = holder.getSendProxyRegister();
                        register.registerFairMethodObject(methodWrapper, bean);
                    }
                }
            }
        }

        MqttContextRegister register = MqttContextRegister.getInstance();
        Map<String, RegisterHolder> contextCache = register.getContextCache();
        for (String name : contextCache.keySet()) {
            RegisterHolder holder = contextCache.get(name);
            MqttContext context = holder.getContext();
            IoLog ioLog = context.getLog();
            ioLog.debug("Aop component loading mqtt context: {} completed", name);
            ioLog.trace(holder);
        }
        register.start();
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }


}
