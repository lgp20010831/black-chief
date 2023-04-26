package com.black.nio.code.util;

import com.black.nio.code.ChannelHandler;
import com.black.nio.code.Configuration;
import com.black.nio.code.NioServerContext;
import lombok.NonNull;

import java.util.function.Consumer;

public class NioBuilder {

    public static final int DEFAULT_PORT = 5000;

    public static NioServerContext create(@NonNull ChannelHandler channelHandler){
        return create(DEFAULT_PORT, channelHandler);
    }

    public static NioServerContext create(@NonNull int port, @NonNull ChannelHandler channelHandler){
        return create("0.0.0.0", port, channelHandler);
    }

    public static NioServerContext create(@NonNull String host, @NonNull int port, @NonNull ChannelHandler channelHandler){
        return create(host, port, channelHandler, null);
    }

    public static NioServerContext create(@NonNull String host, @NonNull int port, @NonNull ChannelHandler channelHandler, Consumer<Configuration> consumer){
        Configuration configuration = new Configuration();
        configuration.setHost(host);
        configuration.setPort(port);
        configuration.setChannelInitialization(pipeline -> {
            pipeline.addLast(channelHandler);
        });
        if (consumer != null){
            consumer.accept(configuration);
        }
        return new NioServerContext(configuration);
    }
}
