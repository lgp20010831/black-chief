package com.black.pool;

public interface Pool<E> {

    E getElement();

    Configuration getConfiguration();

    int getSort();

    void shutdown();
}
