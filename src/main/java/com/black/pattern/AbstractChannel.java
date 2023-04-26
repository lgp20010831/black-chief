package com.black.pattern;

import java.io.IOException;

public abstract class AbstractChannel<T, E> implements Channel<T, E>{

    final T target;

    protected AbstractChannel(T target) {
        this.target = target;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public Pipeline<NodeAdaptation<T>, T, T> toPipeline() {
        throw new UnsupportedOperationException("to pipeline");
    }
}
