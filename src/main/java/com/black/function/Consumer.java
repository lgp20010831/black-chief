package com.black.function;

import java.util.Objects;

public interface Consumer<T> {

    void accept(T t) throws Throwable;

    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
