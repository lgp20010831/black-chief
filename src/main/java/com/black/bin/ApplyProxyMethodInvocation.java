package com.black.bin;

import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class ApplyProxyMethodInvocation implements MethodInvocation {

    private final ProxyTemplate proxyTemplate;

    private final Object[] args;

    public static MethodInvocation wrapper(ProxyTemplate template, Object[] args){
        return new ApplyProxyMethodInvocation(template, args);
    }

    public ApplyProxyMethodInvocation(ProxyTemplate proxyTemplate, Object[] args) {
        this.proxyTemplate = proxyTemplate;
        this.args = args;
    }

    @NotNull
    @Override
    public Method getMethod() {
        return proxyTemplate.getMethod();
    }

    @NotNull
    @Override
    public Object[] getArguments() {
        return args;
    }

    @Nullable
    @Override
    public Object proceed() throws Throwable {
        return proxyTemplate.invokeOriginal(getArguments());
    }

    @Nullable
    @Override
    public Object getThis() {
        return proxyTemplate.getBean();
    }

    @NotNull
    @Override
    public AccessibleObject getStaticPart() {
        return getMethod();
    }
}
