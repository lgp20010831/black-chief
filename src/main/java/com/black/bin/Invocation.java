package com.black.bin;

import com.black.core.tools.BeanUtil;

import java.lang.reflect.Method;

public class Invocation {

    private final ProxyTemplate template;

    private final Object[] args;

    public Invocation(ProxyTemplate template, Object[] args) {
        this.template = template;
        this.args = args;
    }

    public ProxyTemplate getTemplate() {
        return template;
    }

    public Object getThis(){
        return template.getBean();
    }

    public Object getProxy(){
        return template.getProxyBean();
    }

    public Object[] getArgs(){
        return args;
    }

    public Class<?> getType(){
        return BeanUtil.getPrimordialClass(getThis());
    }

    public Method getMethod(){
        return template.getMethod();
    }

    public Object invoke() throws Throwable {
        return template.invokeOriginal(getArgs());
    }

    public Object invoke(Object[] args) throws Throwable {
        return template.invokeOriginal(args);
    }
}
