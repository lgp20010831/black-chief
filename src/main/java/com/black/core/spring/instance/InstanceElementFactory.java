package com.black.core.spring.instance;

public interface InstanceElementFactory {

    <T> InstanceElement<T> createElement(Class<T> beanClass);


}
