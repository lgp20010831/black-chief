package com.black.monitor;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;

import java.lang.reflect.Method;

@SuppressWarnings("all")
public class MonitorApplyProxy<K, V> implements ApplyProxyLayer {

    private final Monitor<K, V> monitor;

    public MonitorApplyProxy(Monitor<K, V> monitor) {
        this.monitor = monitor;
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        return null;
    }
}
