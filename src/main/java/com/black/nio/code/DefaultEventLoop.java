package com.black.nio.code;

import com.black.throwable.IOSException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Set;

public class DefaultEventLoop extends AbstractEventLoop{

    public DefaultEventLoop(Configuration configuration, EventLoopGroup loopGroup) throws IOException {
        super(loopGroup, configuration);
    }

    protected void processorKey(SelectionKey key) throws IOException {
        NioChannel nioChannel = (NioChannel) key.attachment();
        try {

            if (key.isReadable()){
                if (nioChannel != null){
                    ((AbstractNioChannel)nioChannel).readChannel();
                }else {
                    key.cancel();
                }
                return;
            }

            if (key.isWritable()){
                if (nioChannel != null){
                    ((AbstractNioChannel)nioChannel).writeChannel(key);
                }else {
                    key.cancel();
                }
            }
        }catch (IOException ioe){
            //处理 io 事件发生的异常
            nioChannel.error(ioe);
            throw ioe;
        }catch ( IOSException ios){
            Throwable cause = ios.getCause();
            nioChannel.error(cause);
            if (cause instanceof IOException){
                throw (IOException) cause;
            }else {
                throw new IOException(cause);
            }
        }
    }

    @Override
    public void handlerIoEvent() throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey key : selectionKeys) {
            if (key.isValid()){
                processorKey(key);
            }
        }
    }
}
