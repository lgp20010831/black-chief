package com.black.core.factory;


import com.black.utils.ReflexHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractCacheFactory<K, V> implements CacheFactory<K, V> {

    protected final Map<K, V> source;

    protected final String id;

    protected Factory parent;

    protected AbstractCacheFactory(){
        this(null);
    }

    protected AbstractCacheFactory(Factory parent) {
        this.parent = parent;
        source = obtainMapType();
        id = obtainId();
    }


    protected String obtainId(){
        return UUID.randomUUID().toString();
    }

    //此方法子类重写, 规定 source 的类型
    protected Map<K, V> obtainMapType(){
        return new HashMap<>();
    }

    public String id() {
        return id;
    }

    public Map<K, V> getSource(){
        return source;
    }


    @Override
    public void registerBean(K key, V bean) {
        if (containBran(key)) {
            Object obj = source.get(key);
            if (obj instanceof Collection){
                Collection<Object> collection = (Collection<Object>) obj;
                collection.add(bean);
            }else {

                //否则将会替换掉 存在的 bean
                source.put(key, bean);
            }
        }else {
            source.put(key, bean);
        }
    }

    @Override
    public V remove(K key) {
        return source.remove(key);
    }

    @Override
    public boolean containBran(K key) {
        return source.containsKey(key);
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public void merga(CacheFactory<K,V> otherFactory) {
        Map<K, V> source = otherFactory.getSource();
        doMerge(source);
    }

    @Override
    public CacheFactory<K, V> convert(Object similarFactoryObject) throws ConvertCacheFactoryException {
        if (similarFactoryObject instanceof  CacheFactory<?, ?>){
            CacheFactory<?, ?> cacheFactory = (CacheFactory) similarFactoryObject;
            Map<?, ?> source = cacheFactory.getSource();
            //获取泛型
            Class<?>[] targetGenericVal = ReflexHandler.genericVal(source.getClass(), Map.class);
            Class<? extends AbstractCacheFactory> selfClazz = getClass();
            Class<?>[] selfGenericVal = ReflexHandler.genericVal(selfClazz, Map.class);
            for (int i = 0; i < selfGenericVal.length; i++) {
                if (!selfGenericVal[i].isAssignableFrom(targetGenericVal[i])) {
                    throw new ConvertCacheFactoryException("目标 cacheFactory 类型不匹配");
                }
            }

            //实现转换
            doConvert((Map<K, V>) source);
        }else if (similarFactoryObject instanceof Map){
            Map<?, ?> map = (Map<?, ?>) similarFactoryObject;
            doConvert((Map<K, V>) map);
        }
        return this;
    }

    @Override
    public void clear() {
        source.clear();
    }

    //需子类重写
    protected abstract void doConvert(Map<K, V> targetSource);

    protected abstract void doMerge(Map<K, V> targetSource);

    @Override
    public Factory getParent() {
        return parent;
    }
}
