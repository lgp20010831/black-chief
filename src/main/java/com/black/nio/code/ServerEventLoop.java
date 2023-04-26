package com.black.nio.code;

import com.black.nio.code.run.Future;
import com.black.nio.code.util.NioUtils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

@Log4j2
public class ServerEventLoop extends DefaultEventLoop{

    private final AbstractNioServerContext nioServerContext;

    public ServerEventLoop(Configuration configuration,
                           EventLoopGroup loopGroup, AbstractNioServerContext nioServerContext) throws IOException {
        super(configuration, loopGroup);
        this.nioServerContext = nioServerContext;
    }

    @Override
    protected Thread createThread(Runnable runnable) {
        return new Thread(runnable, "nio-server-event-" + threadSort.incrementAndGet());
    }

    @Override
    public void handlerIoEvent() throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey key : selectionKeys) {
            if (key.isValid()) {
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = channel.accept();
                if (log.isDebugEnabled()) {
                    log.debug("接到客户端连接: {}", socketChannel.getRemoteAddress());
                }
                socketChannel.configureBlocking(false);
                NioChannel nioChannel = NioUtils.createNioChannel(socketChannel, getConfiguration());
                Future<NioChannel> future = nioServerContext.getEventLoopGroup().registerChannel(nioChannel, SelectionKey.OP_READ);
                nioChannel.acceptComplete();
            }
        }
    }
}
