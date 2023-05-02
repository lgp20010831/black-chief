package com.black.core.spring.component;

import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.Null;
import com.black.core.spring.driver.FilterComponent;
import com.black.core.spring.driver.PostComponentInstance;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EnableFuryComponent implements PostComponentInstance, FilterComponent {

    private final Map<Class<?>, Annotation> furyAdoptCache = new HashMap<>();
    private final Collection<Class<?>> passComponents = new HashSet<>();
    @Override
    public Object afterInstance(Class<?> openComponentClass, Object openComponent, ChiefExpansivelyApplication expansivelyApplication) {
        if (EnabledControlRisePotential.class.isAssignableFrom(openComponentClass)){
            if (passComponents.contains(openComponentClass)){
                return openComponent;
            }
            EnabledControlRisePotential controlRisePotential = (EnabledControlRisePotential) openComponent;
            Annotation furyAnnotation;
            if (furyAdoptCache.containsKey(openComponentClass)){
                furyAnnotation = furyAdoptCache.get(openComponentClass);
            }else {
                Class<? extends Annotation> annotation = controlRisePotential.registerEnableAnnotation();
                if (annotation == null){
                    throw new IllegalStateException("EnabledControlRisePotential component point anntotation is null, component is :" + controlRisePotential);
                }
                furyAnnotation = hasFuryAnnotation(annotation, expansivelyApplication);
            }
            if (furyAnnotation == null){
                return null;
            }else {
                controlRisePotential.postVerificationQualifiedDo(furyAnnotation, expansivelyApplication);
                passComponents.add(openComponentClass);
                return openComponent;
            }
        }
        return openComponent;
    }

    @Override
    public boolean filter(Class<?> clazz, ChiefExpansivelyApplication application) {
        LazyLoading lazyLoading;
        if ((lazyLoading = AnnotationUtils.getAnnotation(clazz, LazyLoading.class)) != null){
            Class<? extends Annotation> value = lazyLoading.value();
            if (value == null || Null.class.equals(value)){
                return false;
            }
            Annotation annotation = hasFuryAnnotation(value, application);
            if (annotation != null){
                furyAdoptCache.put(clazz, annotation);
                return false;
            }
            return true;
        }
        return false;
    }


    protected Annotation hasFuryAnnotation(Class<? extends Annotation> annotation, ChiefExpansivelyApplication application){
        return ChiefApplicationRunner.getAnnotation(annotation);
    }
}
