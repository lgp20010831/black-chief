package com.black.core.aop.servlet;

import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class ThrowableAvoidConfiguration {

    private final Class<?> targetClass;
    private final Set<Class<? extends Throwable>> errorAvoidSources = new HashSet<>();
    private final String message;

    public ThrowableAvoidConfiguration(Class<?> targetClass, @NonNull AvoidThrowable avoidThrowable) {
        this.targetClass = targetClass;
        this.message = avoidThrowable.runtimeMessage();
        Collections.addAll(errorAvoidSources, avoidThrowable.value());
    }

}
