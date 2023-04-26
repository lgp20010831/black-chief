package com.black.nio.code;

import com.black.nio.code.util.NioUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

@Log4j2
public abstract class AbstractNioServerContext extends AbstractNioContext{

    private EventLoopGroup serverLoopGroup;

    private NioChannel nioChannel;

    public AbstractNioServerContext(@NonNull Configuration configuration) {
        super(configuration);
    }

    @Override
    public void start() throws IOException {
        eventLoopGroup = createEventLoopGroup();
        Configuration configuration = getConfiguration();
        try {

            //创建一个 server 线程组
            serverLoopGroup = new ServerEventLoopGroup(configuration);

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            log.info("server 监听地址:{} | 端口:{}", configuration.getHost(), configuration.getPort());
            serverSocketChannel.bind(configuration.getAddress());

            nioChannel = NioUtils.createNioChannel(serverSocketChannel, configuration);
            //注册一个 server channel
            serverLoopGroup.registerChannel(nioChannel, SelectionKey.OP_ACCEPT);
        }catch (IOException e){
            throw new AttysNioException(e);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        serverLoopGroup.close();
    }

    @Override
    public void shutdownNow() {
        super.shutdownNow();
        serverLoopGroup.close();
    }

    @Override
    protected EventLoopGroup createEventLoopGroup() throws IOException {
        return new DefaultEventLoopGroup(getConfiguration());
    }
}
