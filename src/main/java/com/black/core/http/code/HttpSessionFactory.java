package com.black.core.http.code;

import com.black.core.spring.factory.DefaultProxyFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpSessionFactory {

    private final Configuration configuration;

    static final Map<Class<?>, Object> cache = new HashMap<>();

    public HttpSessionFactory(){
        this(new Configuration());
    }

    public HttpSessionFactory(Configuration configuration) {
        this.configuration = configuration;
        if (configuration.getProxyFactory() == null){
            configuration.setProxyFactory(new DefaultProxyFactory());
        }
    }

    public <T> T openSession(Class<T> mapperClass){
        if (!mapperClass.isInterface()){
            throw new HttpsException("proxy class must is interface");
        }

        if (!cache.containsKey(mapperClass)){
            T proxy = configuration.getProxyFactory().proxy(mapperClass, new HttpProxyLayer());
            cache.put(mapperClass, proxy);
        }

        return (T) cache.get(mapperClass);
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
