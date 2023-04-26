package com.black.function;

public interface Callback<T> {


    void call(T t) throws Throwable;
}
