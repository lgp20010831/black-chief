package com.black.core.factory.beans.imports;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.process.inter.BeanLifeCycleProcessor;
import com.black.core.factory.beans.process.inter.BeanMethodHandler;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.ExecutableWrapper;
import com.black.core.query.MethodWrapper;
import lombok.extern.log4j.Log4j2;

/**
 * @author 李桂鹏
 * @create 2023-05-23 10:58
 */
@SuppressWarnings("all") @Log4j2
public class ImportBeanProcessor implements BeanMethodHandler, BeanLifeCycleProcessor {

    @Override
    public boolean support(ExecutableWrapper ew, ParameterWrapper parameter, Object bean) {
        return ew.hasAnnotation(Import.class);
    }

    @Override
    public Object beforeBeanInstance(BeanDefinitional<?> definitional, BeanFactory beanFactory, Object chainPreviousBean) throws Throwable {
        ClassWrapper<?> classWrapper = definitional.getClassWrapper();
        Import annotation = classWrapper.getAnnotation(Import.class);
        if (annotation != null){
            importsBean(annotation, beanFactory);
        }
        return chainPreviousBean;
    }

    protected void importsBean(Import annotation, BeanFactory beanFactory){
        Class<?>[] classes = annotation.value();
        for (Class<?> type : classes) {
            try {
                beanFactory.getSingleBean(type);
            }catch (BeanFactorysException ex){
                log.info("An error occurred while importing other dependencies, " +
                        "the dependencies being imported: {}", type.getSimpleName());
                throw new BeanFactorysException("An error occurred while importing other dependencies, " +
                        "the dependencies being imported: " + type.getSimpleName(), ex);
            }

        }
    }

    @Override
    public Object handler(MethodWrapper method, ParameterWrapper parameter, Object bean, BeanFactory factory, Object previousValue) {
        Import annotation = method.getAnnotation(Import.class);
        if (annotation != null){
            importsBean(annotation, factory);
        }
        return previousValue;
    }

    @Override
    public Object structure(ConstructorWrapper<?> cw, ParameterWrapper pw, BeanFactory factory, Object previousValue) {
        Import annotation = cw.getAnnotation(Import.class);
        if (annotation != null){
            importsBean(annotation, factory);
        }
        return previousValue;
    }
}
