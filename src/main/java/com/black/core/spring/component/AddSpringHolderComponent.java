package com.black.core.spring.component;

import com.black.core.util.StringUtils;
import com.black.holder.SpringHodler;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.driver.PostComponentInstance;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

public class AddSpringHolderComponent implements PostComponentInstance {

    @Override
    public Object afterInstance(Class<?> openComponentClass, Object openComponent, ChiefExpansivelyApplication expansivelyApplication) {

        AddHolder addHolder;
        if ((addHolder = AnnotationUtils.getAnnotation(openComponentClass, AddHolder.class)) != null){
            String beanName = "".equals(addHolder.value()) ? StringUtils.titleLower(openComponentClass.getSimpleName()) : addHolder.value();
            DefaultListableBeanFactory beanFactory = SpringHodler.getListableBeanFactory();
            if (beanFactory != null){
                try {
                    if (!beanFactory.containsBean(beanName)) {
                        beanFactory.registerSingleton(beanName, openComponent);
                    }
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                }
            }
        }
        return openComponent;
    }
}
