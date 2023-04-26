package com.black.core.data;

import java.util.Collection;

public interface TransferStation {

    String getName();

    void push(Data<?> data);

    void registerConsumer(DataConsumer consumer);

    Collection<DataConsumer> getConsumers();

    void removeConsumer(DataConsumer consumer);

    void shutdown();
}
