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

@Log4j2
public class ChiefApplicationRunner implements GenericApplicationListener {

    private static Boolean open = null;

    private static SpringApplication springApplication;

    private static Class<?> mainClass;

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
        springApplication = application;
        mainClass = application.getMainApplicationClass();
        if (open == null){
            open = AnnotationUtils.getAnnotation(mainClass, OpenChiefApplication.class) != null;
        }
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
        Class<?> mc = getMainClass();
        if (mc == null) return false;
        return AnnotationUtils.getAnnotation(mc, type) != null;
    }


}
