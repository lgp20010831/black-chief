package com.black.function;

@FunctionalInterface
public interface Production<R> {


    R create(Object... science);
}
