package com.black.core.factory;


import java.util.Map;

//提供缓存的概念
//并不具有创建的能力
@SuppressWarnings("all")
public interface CacheFactory<K, V> extends Factory {

    //注册一个 bean
    void registerBean(K key, V bean);

    //获取 bean 对象
    V getBean(K key);

    //清除一个 bean
    V remove(K key);

    //是否包含目标 bean
    boolean containBran(K key);

    //当前 bean 容量
    int size();

    //合并一个缓存factory
    void merga(CacheFactory<K, V> otherFactory);


    //将一个类似 facory 的对象转换成一个缓存factory
    CacheFactory<K, V> convert(Object similarFactoryObject) throws ConvertCacheFactoryException;

    //清理缓存
    void clear();

    //获取所有资源
    Map<K, V> getSource();
}
