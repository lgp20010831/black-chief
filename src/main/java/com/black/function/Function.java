package com.black.function;

public interface Function<T, R> {

    R apply(T t) throws Throwable;

}
