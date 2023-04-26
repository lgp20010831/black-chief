package com.black.pattern;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;

import java.lang.reflect.AnnotatedElement;

public class PremiseManager {


    public static boolean premise(AnnotatedElement element){
        if (element instanceof Class<?>){
            return premise((Class<?>) element);
        }
        if (element.isAnnotationPresent(PremiseProxy.class)){
            PremiseProxy annotation = element.getAnnotation(PremiseProxy.class);
            Class<? extends Premise> premiseType = annotation.value();
            return doPremise(premiseType);
        }
        return true;
    }

    public static boolean premise(Class<?> type){
        if (Premise.class.isAssignableFrom(type)){
            return doPremise((Class<? extends Premise>) type);
        }

        if (type.isAnnotationPresent(PremiseProxy.class)){
            PremiseProxy annotation = type.getAnnotation(PremiseProxy.class);
            Class<? extends Premise> premiseType = annotation.value();
            return doPremise(premiseType);
        }
        return true;
    }


    private static boolean doPremise(Class<? extends Premise> type){
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        Premise premise = beanFactory.prototypeCreateBean(type);
        return premise.premise();
    }
}
