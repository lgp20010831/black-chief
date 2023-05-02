package com.black.core.spring;

import com.black.core.annotation.OpenChiefApplication;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class ChiefApplicationRunner implements GenericApplicationListener {

    private static Boolean open = null;

    private static SpringApplication springApplication;

    private static Class<?> mainClass;

    private final static Set<Class<?>> mainClasses = new HashSet<>();

    public static void openChiefApplication(){
        open = true;
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return ApplicationContextInitializedEvent.class.isAssignableFrom(eventType.resolve());
    }

    public static SpringApplication getSpringApplication() {
        return springApplication;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        SpringApplication application = (SpringApplication) event.getSource();
        loadMain(application);
        if (open == null){
            open = isPertain(OpenChiefApplication.class);
        }
    }

    public static void loadMain(SpringApplication application){
        Set<Object> allSources = application.getAllSources();
        for (Object source : allSources) {
            if (source instanceof Class){
                mainClasses.add((Class<?>) source);
            }
        }
        springApplication = application;
        mainClass = application.getMainApplicationClass();
        mainClasses.add(mainClass);
    }

    public static Set<Class<?>> getMainClasses() {
        return mainClasses;
    }

    public static Class<?> getMainClass() {
        return mainClass;
    }

    public static boolean isOpen(){
        if (open == null){
            return false;
        }
        return open;
    }

    public static boolean isPertain(Class<? extends Annotation> type){
        for (Class<?> clazz : getMainClasses()) {
            if (AnnotationUtils.getAnnotation(clazz, type) != null){
                return true;
            }
        }
        return false;
    }

    public static <T extends Annotation> T getAnnotation(Class<T> type){
        for (Class<?> clazz : getMainClasses()) {
            T annotation = AnnotationUtils.getAnnotation(clazz, type);
            if (annotation != null){
                return annotation;
            }
        }
        return null;
    }

}
