package com.black.pattern;

import com.black.core.query.MethodWrapper;
import com.black.core.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodInvoker {

    private final MethodWrapper mw;

    private Object invokeBean;

    public MethodInvoker(Object element) {
        if (element instanceof MethodWrapper){
            mw = (MethodWrapper) element;
        }else if (element instanceof Method){
            mw = MethodWrapper.get((Method) element);
        }else {
            throw new IllegalStateException("ill element is not mehtod");
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> type){
        return mw.getAnnotation(type);
    }

    public MethodWrapper getMw() {
        return mw;
    }

    public void setInvokeBean(Object invokeBean) {
        this.invokeBean = invokeBean;
    }

    public Object getInvokeBean() {
        return invokeBean;
    }

    public Object invoke(Object... args){
        Assert.notNull(invokeBean, "invoke bean is null");
        checkBean();
        return mw.invoke(invokeBean, args);
    }

    protected void checkBean(){
        if (invokeBean instanceof LazyBean){
            invokeBean = ((LazyBean) invokeBean).getBean();
        }
    }

    @Override
    public String toString() {
        return "[invoker: " + invokeBean + " --> method: " + mw.getName() + "]";
    }
}
