package com.black.nio.code;

import com.black.nio.code.run.Future;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;

@Log4j2
public class ClientEventLoop extends DefaultEventLoop{

    public ClientEventLoop(Configuration configuration, EventLoopGroup loopGroup) throws IOException {
        super(configuration, loopGroup);
    }


    @Override
    protected Thread createThread(Runnable runnable) {
        return new Thread(runnable, "nio-client-event-" + threadSort.incrementAndGet());
    }

    protected void processorConnect(SelectionKey key){
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.finishConnect()) {
                if (log.isDebugEnabled()) {
                    log.debug("成功连接服务端");
                }
                NioChannel nioChannel = (NioChannel) key.attachment();
                Future<NioChannel> future = getConfiguration().getContext().getEventLoopGroup().registerChannel(nioChannel, SelectionKey.OP_READ);
                nioChannel.connectComplete();
            }
        }catch (IOException ioe){
            //连接拒绝后, 应该怎么做
            NioChannel attachment = (NioChannel) key.attachment();
            attachment.error(ioe);
        }
    }

    @Override
    public void handlerIoEvent() throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey key : selectionKeys) {
            if (key.isValid()) {
                if (key.isConnectable()) {
                    processorConnect(key);
                }else {
                    processorKey(key);
                }
            }
        }
    }

}
