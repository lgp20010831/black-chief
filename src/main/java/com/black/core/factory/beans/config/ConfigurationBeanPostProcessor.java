package com.black.core.factory.beans.config;

import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.process.inter.BeanLifeCycleProcessor;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Log4j2
public class ConfigurationBeanPostProcessor implements BeanLifeCycleProcessor {


    @Override
    public boolean interceptBeanInstance(BeanDefinitional<?> definitional, BeanFactory beanFactory) {
        //Prototype objects are not processed
        if (definitional.isPrototype()) {
            return false;
        }
        Class<?> primordialClass = definitional.getPrimordialClass();
        ConfigurationBean annotation = AnnotationUtils.getAnnotation(primordialClass, ConfigurationBean.class);
        if (annotation != null){
            Condition condition = AnnotationUtils.getAnnotation(primordialClass, Condition.class);
            if (condition != null){
                BeanCondition beanCondition = instanceCondition(condition.value(), beanFactory);
                return !beanCondition.establish();
            }
        }
        return BeanLifeCycleProcessor.super.interceptBeanInstance(definitional, beanFactory);
    }

    @Override
    public Object beforeBeanInstance(BeanDefinitional<?> definitional, BeanFactory beanFactory, Object chainPreviousBean) {
        //Prototype objects are not processed
        if (definitional.isPrototype()) {
            return chainPreviousBean;
        }
        Class<?> primordialClass = definitional.getPrimordialClass();
        ConfigurationBean annotation = AnnotationUtils.getAnnotation(primordialClass, ConfigurationBean.class);
        if (annotation != null){
            if (log.isDebugEnabled()) {
                log.debug("Configuration class object detected, " +
                        "target bean class: [{}][", definitional.getBeanName());
            }
            beforeConfigurationBeanInstance(definitional, beanFactory, chainPreviousBean);
        }
        return chainPreviousBean;
    }


    protected void beforeConfigurationBeanInstance(BeanDefinitional<?> definitional, BeanFactory beanFactory, Object chainPreviousBean){
        Class<?> primordialClass = definitional.getPrimordialClass();
        PriorityCreationBeforeLoading priorityLoading = AnnotationUtils.getAnnotation(primordialClass, PriorityCreationBeforeLoading.class);
        if (priorityLoading != null){
            if (chainPreviousBean != null){
                if (log.isWarnEnabled()) {
                    log.warn("When the configuration class needs to load other objects first, " +
                            "but it has been instantiated by other processors, configuration bean: [{}]",
                            definitional.getBeanName());
                }
            }
            priorityLoadingClasses(priorityLoading.value(), definitional, beanFactory);
        }
    }

    public BeanCondition instanceCondition(@NonNull Class<? extends BeanCondition> conditionClass, BeanFactory beanFactory){
        Constructor<? extends BeanCondition> constructor;
        try {
             constructor = conditionClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new BeanFactorysException("Beancondition needs to be provided without " +
                    "parameter construction, conditionClass is [" + conditionClass.getSimpleName() + "]");
        }

        BeanCondition beanCondition;
        try {
             beanCondition = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BeanFactorysException("Exception occurred in instantiation BeanCondition, " +
                    "conditionClass is [" + conditionClass.getSimpleName() + "]");
        }
        beanFactory.autoWriedBean(beanCondition);
        return beanCondition;
    }

    protected void priorityLoadingClasses(Class<?>[] classes, BeanDefinitional<?> definitional, BeanFactory beanFactory){
        for (Class<?> beforeLoadClass : classes) {
            try {

                beanFactory.getSingleBean(beforeLoadClass);
            }catch (BeanFactorysException bfe){
                if (log.isErrorEnabled()) {
                    log.error("When loading the configuration class, " +
                                    "because it needs to load other classes " +
                                    "first, an exception occurs in loading other " +
                                    "classes, configuration bean: [{}], loading bean: [{}]",
                            definitional.getBeanName(), beforeLoadClass.getSimpleName());
                }
                throw new BeanFactorysException("When loading the configuration class, " +
                        "because it needs to load other classes " +
                        "first, an exception occurs in loading other " +
                        "classes, configuration bean: [" + definitional.getBeanName() + "], " +
                        "loading bean: [" + beforeLoadClass.getSimpleName() + "]", bfe);
            }
        }
    }

    @Override
    public void aboutFactory(Object bean, BeanFactory beanFactory) {
        BeanDefinitional<?> definitional = beanFactory.getDefinitional(bean);
        if (definitional == null){
            throw new BeanFactorysException("Unable to find the identity definition " +
                    "of the molding object, which may be affected by multithreading. " +
                    "Please keep thread safe, forming object is [" + bean + "]");
        }

        //Prototype objects are not processed
        if (definitional.isPrototype()) {
            return;
        }
        Class<?> primordialClass = definitional.getPrimordialClass();
        ClassWrapper<?> classWrapper = definitional.getClassWrapper();
        ConfigurationBean annotation = AnnotationUtils.getAnnotation(primordialClass, ConfigurationBean.class);
        if (annotation != null){
            for (MethodWrapper method : classWrapper.getMethods()) {
                final Method primordialMethod = method.getMethod();
                final String name = primordialMethod.getName();
                ProvideBean provideBean = AnnotationUtils.getAnnotation(primordialMethod, ProvideBean.class);
                if (provideBean != null){

                    Condition condition = AnnotationUtils.getAnnotation(primordialMethod, Condition.class);
                    if (condition != null){
                        if (log.isDebugEnabled()) {
                            log.debug("When scanning the object providing method " +
                                    "of the configuration class, it is found that " +
                                    "conditional processing is required to load this " +
                                    "object,  configuration bean: [{}], loading method: [{}]",
                                    definitional.getBeanName(), name);
                        }
                        BeanCondition beanCondition = instanceCondition(condition.value(), beanFactory);
                        if (!beanCondition.establish()){
                            if (log.isDebugEnabled()) {
                                log.debug("When loading the configuration class method, " +
                                        "the method condition does not hold. This object " +
                                        "will not be loaded,  configuration bean: [{}], " +
                                        "loading method: [{}]", definitional.getBeanName(), name);
                            }
                            continue;
                        }
                    }
                    //no condition
                    Object beanResult = beanFactory.invokeBeanMethod(bean, method);
                    if (beanResult == null){
                        throw new BeanFactorysException("The configuration class load object " +
                                "method cannot accept an empty object as a bean, " +
                                "configuration bean: [" + definitional.getBeanName() + "], loading method: [" + name + "]");
                    }
                    Class<Object> methodBeanClass = BeanUtil.getPrimordialClass(beanResult);
                    if (beanFactory.containBean(methodBeanClass)) {
                        throw new BeanFactorysException("The object returned by the configuration class " +
                                "loading object already has an object of the same type in the factory, " +
                                "configuration bean: [" + definitional.getBeanName() + "], loading method: [" + name + "]");
                    }
                    beanFactory.registerBean(methodBeanClass);
                }
            }
        }
    }
}
