package com.black.core.ill.aop;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import lombok.Getter;

import java.lang.reflect.Method;

//发生异常
@Getter
public class IllSourceWrapper {

    private final ClassWrapper<?> primordialClass;

    private final MethodWrapper methodWrapper;

    private final Object bean;

    private Throwable error;

    private final ThrowableCaptureConfiguration configuration;

    private IllSourceWrapper sourceWrapper;

    private Object[] args;

    public IllSourceWrapper(Object bean, Method method, ThrowableCaptureConfiguration configuration){
        this.bean = bean;
        this.methodWrapper = MethodWrapper.get(method);
        this.primordialClass = ClassWrapper.get(BeanUtil.getPrimordialClass(bean));
        this.configuration = configuration;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void setSourceWrapper(IllSourceWrapper sourceWrapper) {
        this.sourceWrapper = sourceWrapper;
    }
}
