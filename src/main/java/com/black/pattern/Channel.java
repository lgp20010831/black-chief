package com.black.pattern;

import java.io.IOException;
import java.io.InputStream;

public interface Channel<T, E> {

    T getTarget();

    void close() throws IOException;

    boolean isOpen();

    void write(E e);

    InputStream getInputStream();

    Pipeline<NodeAdaptation<T>, T, T> toPipeline();
}
