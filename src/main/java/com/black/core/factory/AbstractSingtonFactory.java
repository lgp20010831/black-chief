package com.black.core.factory;



public abstract class AbstractSingtonFactory<K, V> extends AbstractCacheFactory<K, V> implements SingtonCacheFactory<K, V>{

    public AbstractSingtonFactory() {
    }

    public AbstractSingtonFactory(Factory parent) {
        super(parent);
    }


}
