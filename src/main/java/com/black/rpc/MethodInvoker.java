package com.black.rpc;

import com.black.core.query.MethodWrapper;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class MethodInvoker {

    private final MethodWrapper mw;

    private final Object bean;

    private final String methodName;

    public MethodInvoker(@NonNull MethodWrapper mw, @NonNull Object bean, @NonNull String methodName) {
        this.mw = mw;
        this.bean = bean;
        this.methodName = methodName;
    }

    public Object invoke(Object[] args) throws Throwable{
        return mw.invoke(bean, args);
    }
}
