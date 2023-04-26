package com.black.core.factory;

//单例缓存工厂
@SuppressWarnings("all")
public interface SingtonCacheFactory<K, V> extends CacheFactory<K, V>{


    @Override
    default V getBean(K key) {
        return getSington(key);
    }

    //根据 key 获取单例对象
    V getSington(K key);




}
