package com.black.core.ill.aop;

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class ThrowableCaptureConfiguration {

    private final Method method;

    private boolean writeStackHeap;

    private boolean ifUnattendedCapture;

    private String captureResult;

    //如果 ifUnattendedCapture 是个条目, 则此属性忽略
    private Class<?> resultType;

    public ThrowableCaptureConfiguration(Method method) {
        this.method = method;
    }
}
