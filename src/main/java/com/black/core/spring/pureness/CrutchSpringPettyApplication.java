package com.black.core.spring.pureness;

import com.black.bin.InstanceType;
import com.black.core.aop.listener.GlobalAopRunnerListener;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ApplicationHolder;
import com.black.core.spring.PettyApplicationConfiguration;
import com.black.core.spring.PettySpringApplication;
import com.black.core.spring.driver.PostBeanAfterInitializationDriver;
import com.black.core.spring.driver.PostBeanFactoryDriver;
import com.black.core.spring.driver.PostBeanRegisterDriver;
import com.black.core.spring.driver.PostBeforeBeanInstantiationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.spring.instance.LightnessInstanceFactory;
import com.black.utils.ProxyUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.Phased;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;
import java.util.Map;

@Log4j2
public class CrutchSpringPettyApplication extends PettySpringApplication implements ApplicationListener<ContextRefreshedEvent>,
        InstantiationAwareBeanPostProcessor, BeanDefinitionRegistryPostProcessor, DisposableBean, Phased, SmartLifecycle {

    /** spring application */
    protected ApplicationContext applicationContext;

    /** beanFactory */
    protected DefaultListableBeanFactory beanFactory;

    public CrutchSpringPettyApplication(){}

    public CrutchSpringPettyApplication(Object configuration) {
        super(configuration);
    }

    public void setBeanFactory(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public DefaultListableBeanFactory getBeanFactory() {
        if (beanFactory == null){
            return FactoryManager.getBeanFactory().getSingleBean(DefaultListableBeanFactory.class);
        }
        return beanFactory;
    }

    @Override
    protected InstanceType selectInstanceType() {
        return InstanceType.BEAN_FACTORY_SINGLE;
    }

    @Override
    public int getPhase() {
        return SmartLifecycle.DEFAULT_PHASE;
    }


    public void registerBeanInSpring(String beanName, Object bean) {
        DefaultListableBeanFactory beanFactory = getBeanFactory();
        beanFactory.registerSingleton(beanName, bean);
        beanFactory.autowireBean(bean);
    }


    /**
     * 获取 spring 项目中的启动类
     * @return 返回启动类 class 对象
     */
    public Class<?> getStartUpClazz(){

        if (mainClass != null){
            return mainClass;
        }

        SpringApplication springApplication = GlobalAopRunnerListener.getSpringApplication();
        if (springApplication != null){
            mainClass = springApplication.getMainApplicationClass();
        }
        if (mainClass != null){
            return mainClass;
        }
        if (applicationContext == null){
            return null;
        }
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        if (beansWithAnnotation.isEmpty())
            return null;

        if (beansWithAnnotation.size() > 1){
            return applicationConfiguration.getSpringBootStartUpClazz();
        }
        return mainClass = ProxyUtil.getPrimordialClass(beansWithAnnotation.values().toArray()[0]);
    }

    @Override
    protected PettyApplicationConfiguration createConfiguration() {
        return instanceFactory().getInstance(CrutchSpringApplicationConfiguration.class);
    }

    @Override
    protected void initFactory() {
        super.initFactory();
        if (ApplicationHolder.getApplicationContext() != null){
            instanceFactory.registerInstance(ApplicationContext.class, ApplicationHolder.getApplicationContext());
            setApplicationContext(ApplicationHolder.getApplicationContext());
        }
        if (ApplicationHolder.getBeanFactory() != null){
            instanceFactory.registerInstance(BeanFactory.class, ApplicationHolder.getBeanFactory());
            if (ApplicationHolder.getBeanFactory() instanceof DefaultListableBeanFactory){
                instanceFactory.setSpringBeanFactory((DefaultListableBeanFactory) ApplicationHolder.getBeanFactory());
            }
            setBeanFactory((DefaultListableBeanFactory) ApplicationHolder.getBeanFactory());
        }
    }

    @Override
    public InstanceFactory obtainInstanceFactory() {
        InstanceFactory instanceFactory = super.obtainInstanceFactory();
        ((LightnessInstanceFactory)instanceFactory).setBeanFactory(beanFactory);
        return instanceFactory;
    }


    @Override
    protected Map<String, Object> getSpringMutes() {
        return (Map<String, Object>) getBeanFactory().getSingletonMutex();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (driverInstanceCache.containsKey(PostBeanAfterInitializationDriver.class)){
            Collection<PostBeanAfterInitializationDriver> postBeanAfterInitializationDriver = postBeanAfterInitializationDrivers();
            if (!postBeanAfterInitializationDriver.isEmpty()){
                for (PostBeanAfterInitializationDriver postBeanRegisterDriver : postBeanAfterInitializationDriver) {
                    postBeanRegisterDriver.postProcessAfterInitialization(bean, beanName, this);
                }
            }
        }

        return bean;
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        Object bean = null;
        Collection<PostBeforeBeanInstantiationDriver> postBeforeBeanInstantiationDrivers = postBeforeBeanInstantiationDrivers();
        if (!postBeforeBeanInstantiationDrivers.isEmpty()){
            PostBeforeBeanInstantiationDriver previousDriver = null;
            for (PostBeforeBeanInstantiationDriver beforeBeanInstantiationDriver : postBeforeBeanInstantiationDrivers) {
                bean  = beforeBeanInstantiationDriver.postBeforeBeanInstantiationLogic(beanClass,
                        beanName, this, bean, previousDriver);
                previousDriver = beforeBeanInstantiationDriver;
            }
        }
        return bean;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (driverInstanceCache.containsKey(PostBeanFactoryDriver.class)){
            Collection<PostBeanFactoryDriver> postBeanFactoryDriverSet = postBeanFactoryDrivers();
            if (!postBeanFactoryDriverSet.isEmpty()){
                for (PostBeanFactoryDriver postBeanFactoryDriver : postBeanFactoryDriverSet) {
                    postBeanFactoryDriver.postProcessBeanFactory(beanFactory, this);
                }
            }
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (driverInstanceCache.containsKey(PostBeanRegisterDriver.class)){
            Collection<PostBeanRegisterDriver> postBeanRegisterDriverSet = postBeanRegisterDrivers();
            if (!postBeanRegisterDriverSet.isEmpty()){
                for (PostBeanRegisterDriver postBeanRegisterDriver : postBeanRegisterDriverSet) {
                    postBeanRegisterDriver.postProcessBeanDefinitionRegistry(registry, this);
                }
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        doOnApplicationEvent();
    }

    @Override
    public void start() {
        applicationStart();
    }

    @Override
    public void stop() {
        shutdown();
    }

    @Override
    public void stop(Runnable callback) {
        callback.run();
        shutdown();
    }

    @Override
    public boolean isRunning() {
        return !isShutdown();
    }

    @Override
    public void destroy(){
        if (log.isInfoEnabled()) {
            log.info("ChiefExpansivelyApplication destroy...");
        }
    }

}
