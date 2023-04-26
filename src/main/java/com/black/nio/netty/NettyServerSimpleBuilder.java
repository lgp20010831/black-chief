package com.black.nio.netty;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NettyServerSimpleBuilder {

    public static NettyServerContext create(Supplier<ChannelResolver> supplier){
        return create("0.0.0.0", 6666, supplier, null);
    }

    public static NettyServerContext create(Supplier<ChannelResolver> supplier, Consumer<Configuration> consumer){
        return create("0.0.0.0", 6666, supplier, consumer);
    }

    public static NettyServerContext create(int port, Supplier<ChannelResolver> supplier){
        return create("0.0.0.0", port, supplier, null);
    }

    public static NettyServerContext create(int port, Supplier<ChannelResolver> supplier, Consumer<Configuration> consumer){
        return create("0.0.0.0", port, supplier, consumer);
    }

    public static NettyServerContext create(String host, int port, Supplier<ChannelResolver> supplier, Consumer<Configuration> consumer){
        Configuration configuration = new Configuration();
        configuration.setHost(host);
        configuration.setPort(port);
        configuration.setIoEventThreadNum(20);
        configuration.setServerThreadNum(2);
        configuration.setOpenWorkPool(true);
        configuration.setWorkPoolCoreSize(20);
        configuration.setDispatchCallback(dispatch -> {
            ChannelResolver resolver = supplier.get();
            dispatch.addLast(resolver);
        });
        if (consumer != null){
            consumer.accept(configuration);
        }
        return new NettyServerContext(configuration);
    }

}
