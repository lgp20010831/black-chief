package com.black.nio.code.run;

public class DefaultFuture extends AbstractFuture<Object>{

    @Override
    public Object get() {
        return get(-1L);
    }

    @Override
    public Object get(long timeout) {
        throw new UnsupportedOperationException("no result");
    }


}
