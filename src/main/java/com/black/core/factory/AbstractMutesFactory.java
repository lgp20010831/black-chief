package com.black.core.factory;


import java.util.Collection;

public abstract class AbstractMutesFactory<K, V> extends AbstractCacheFactory<K, Collection<V>> implements MutesCacheFactory<K, V>{

    public AbstractMutesFactory() {
        this(null);
    }

    public AbstractMutesFactory(Factory parent) {
        super(parent);
    }



}
