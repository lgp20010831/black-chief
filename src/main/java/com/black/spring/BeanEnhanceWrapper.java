package com.black.spring;

public class BeanEnhanceWrapper {

    private Object bean;

    public BeanEnhanceWrapper(Object bean) {
        this.bean = bean;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}
