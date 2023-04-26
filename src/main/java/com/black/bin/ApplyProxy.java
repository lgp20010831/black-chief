package com.black.bin;

public interface ApplyProxy<A> {

    A getThis();

    Class<A> getType();

    boolean isJdk();
}
