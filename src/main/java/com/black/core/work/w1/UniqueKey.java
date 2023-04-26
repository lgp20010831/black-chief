package com.black.core.work.w1;

public interface UniqueKey<T> {

    T getSource();

    String toDatabaseString();

    UniqueKey<T> addAllUniqueKey(UniqueKey<T> uniqueKey);
}
