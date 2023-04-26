package com.black.core.chain;

import com.black.bin.InstanceType;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.InstanceFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AddHolder
@LoadSort(0) @Log4j2
public class GlobalTaskChainCollector implements OpenComponent, ApplicationDriver {

    private InstanceFactory instanceFactory;
    private ReusingProxyFactory proxyFactory;
    private DefaultListableBeanFactory beanFactory;
    private final Map<CollectedCilent, Collection<ConditionEntry>> clientEntrys = new HashMap<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {

        try {
            instanceFactory = expansivelyApplication.instanceFactory();
            proxyFactory = expansivelyApplication.obtainReusingProxyFactory();
            beanFactory = instanceFactory.getInstance(DefaultListableBeanFactory.class);

            long startTime = System.currentTimeMillis();
            //拿到资源
            Collection<Class<?>> projectClasses = expansivelyApplication.getProjectClasses();
            List<Class<?>> clients = projectClasses.stream()
                    .filter(this::isClient)
                    .collect(Collectors.toList());

            for (Class<?> client : clients) {
                CollectedCilent entrustClient;
                try {
                    entrustClient = instanceClient(client, expansivelyApplication);
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                    log.info("实例化 chain client: [{}] 发生异常: {}", client.getSimpleName(), e.getMessage());
                    continue;
                }
                try {
                    processorClient(entrustClient);
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                    log.info("收集 chain client: [{}] 采集信息时发生异常: {}", client.getSimpleName(), e.getMessage());
                }

            }

            //收集阶段
            projectClasses.forEach(
                    c ->{
                        clientEntrys.forEach((client, entries) ->{
                            for (ConditionEntry entry : entries) {
                                if (entry.getJudge().condition(c)) {
                                    ConditionResultBody resultBody = (ConditionResultBody) entry;
                                    Object instance = null;
                                    if (entry.isInstance()) {
                                        if (entry.isProxy()) {
                                            try {

                                                instance = proxyFactory.proxy(c, entry.getLayer(), instanceFactory);
                                            }catch (Throwable ex){
                                                if (log.isWarnEnabled()) {
                                                    log.warn("Unable to proxy object: [{}], error msg: [{}]",
                                                            c.getSimpleName(), ex.getMessage());
                                                }
                                            }

                                        }else {
                                            String[] names = beanFactory.getBeanNamesForType(c);
                                            if (names.length != 1){
                                                instance = instance(c, entry.getInstanceType());
                                            }else {
                                                try {
                                                    instance = beanFactory.getBean(names[0]);
                                                }catch (BeansException beanEx){
                                                    instance = instance(c, entry.getInstanceType());
                                                }
                                            }

                                        }
                                    }else {
                                        instance = c;
                                    }
                                    if (instance != null){
                                        resultBody.registerObject(instance);
                                    }
                                }
                            }
                        });
                    }
            );

            clientEntrys.forEach((client, entries) ->{
                for (ConditionEntry entry : entries) {
                    client.collectFinish((ConditionResultBody) entry);
                }
            });
            if (log.isInfoEnabled()) {
                log.info("collect: {} ms", System.currentTimeMillis() - startTime);
            }
        }finally {
            clientEntrys.forEach((client, entries) ->{
                for (ConditionEntry entry : entries) {
                    ConditionResultBody resultBody = (ConditionResultBody)entry;
                }
            });
        }
    }

    protected void processorClient(CollectedCilent entrustClient){
        if (entrustClient == null){
            return;
        }
        QueryConditionRegister register = new QueryConditionRegister(entrustClient);
        entrustClient.registerCondition(register);
        Collection<ConditionEntry> entries = register.getEntries();
        clientEntrys.put(entrustClient, entries);
    }


    private Object instance(Class<?> type, InstanceType instanceType){
        try {
            switch (instanceType){
                case REFLEX:
                    return ReflexUtils.instance(type);
                case INSTANCE:
                    return instanceFactory.getInstance(type);
                case BEAN_FACTORY:
                    return FactoryManager.initAndGetBeanFactory().getSingleBean(type);
                default:
                    return null;
            }
        }catch (Throwable e){
            if (log.isWarnEnabled()) {
                log.warn("could not be built object: [{}], error msg: [{}]",
                        type.getSimpleName(), e.getMessage());
            }
            return null;
        }
    }

    protected boolean isClient(Class<?> target){
        if (target.isInterface() || Modifier.isAbstract(target.getModifiers()) || target.isEnum()) {
            return false;
        }
        if (CollectedCilent.class.isAssignableFrom(target)){
            ChainClient client = AnnotationUtils.getAnnotation(target, ChainClient.class);
            if (client != null){
                Class<? extends ChainPremise> value = client.value();
                if (value.equals(ChainPremise.class)){
                    return true;
                }
                ChainPremise premise = instanceFactory.getInstance(value);
                return premise.premise();
            }
        }
        return false;
    }

    protected CollectedCilent instanceClient(Class<?> clazz, ChiefExpansivelyApplication application){
        InstanceFactory factory = application.instanceFactory();
        Adaptation adaptation = AnnotationUtils.getAnnotation(clazz, Adaptation.class);
        if (adaptation == null){
            return (CollectedCilent) factory.getInstance(clazz);
        }else {
            InstanceClientAdapter adapter = factory.getInstance(adaptation.value());
            return adapter.getClient();
        }
    }
}
