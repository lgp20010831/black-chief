package com.black.core.data;

public interface DataConsumer {

    boolean support(Data<?> data);

    void handler(Data<?> data);
}
