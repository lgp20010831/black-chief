package com.black.core.factory.beans.component;

import com.black.core.chain.ChainClient;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.ConditionResultBody;
import com.black.core.chain.QueryConditionRegister;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanFactoryProcessor;
import com.black.core.factory.beans.process.inter.BeanPostProcessor;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.spring.instance.LightnessInstanceFactory;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashSet;

@Log4j2
@LoadSort(17)
@ChainClient  @SuppressWarnings("all")
public class BeanFactoryComponent implements OpenComponent, CollectedCilent {

    public static boolean printFactory = true;
    public static final String NAME = "factory";
    private final DefaultListableBeanFactory springFactory;
    private InstanceFactory instanceFactory;
    private BeanFactory factory;
    private final Collection<Object> beanProcessors = new HashSet<>();
    private final Collection<Object> factoryProcessors = new HashSet<>();

    public BeanFactoryComponent(DefaultListableBeanFactory springFactory) {
        this.springFactory = springFactory;
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        instanceFactory = expansivelyApplication.instanceFactory();
        factory = createBeanFactory();
        instanceFactory.registerInstance(BeanFactory.class, factory);
        if (log.isInfoEnabled() && printFactory) {
            log.info("create bean factory is {}", factory);
        }

        for (Object beanProcessor : beanProcessors) {
            factory.registerBeanLifeCycleProcessor((BeanPostProcessor) beanProcessor);
        }
        for (Object factoryProcessor : factoryProcessors) {
            factory.registerBeanFactoryProcessor((BeanFactoryProcessor) factoryProcessor);
        }
        BeanFactoryHolder.factory = factory;

        if (!factory.containBean(DefaultListableBeanFactory.class)) {
            factory.registerBean(springFactory);
        }

        if (!factory.containBean(LightnessInstanceFactory.class)) {
            factory.registerBean(instanceFactory);
        }

        springFactory.registerSingleton(NAME, factory);
    }

    protected BeanFactory createBeanFactory(){
        FactoryManager.createDefaultBeanFactory();
        return FactoryManager.getBeanFactory();
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("bean", bp ->{
            return BeanPostProcessor.class.isAssignableFrom(bp) &&
                    BeanUtil.isSolidClass(bp) && AnnotationUtils.getAnnotation(bp, BeanProcessor.class) != null;
        });

        register.begin("factory", bp ->{
            return BeanFactoryProcessor.class.isAssignableFrom(bp) &&
                    BeanUtil.isSolidClass(bp) && AnnotationUtils.getAnnotation(bp, FactoryProcessor.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {

        if ("bean".equals(resultBody.getAlias())){
            beanProcessors.addAll(resultBody.getCollectSource());
        }

        if ("factory".equals(resultBody.getAlias())){
            factoryProcessors.addAll(resultBody.getCollectSource());
        }
    }
}
