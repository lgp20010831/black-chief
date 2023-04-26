package com.black.nio.code;

import com.black.utils.IoUtils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class Pipeline {

    private final NioChannel channel;

    private final Configuration configuration;

    private final ChannelHandlerContext head;

    private final ChannelHandlerContext tail;

    public Pipeline(NioChannel channel) {
        this.channel = channel;
        configuration = channel.getConfiguration();
        tail = new DefaultChannelHandlerContext(new Tail(), channel);
        head = new DefaultChannelHandlerContext(new Head(), channel);
        ((AbstractChannelHandlerContext)tail).prev = head;
        ((AbstractChannelHandlerContext)head).next = tail;
    }


    public void addLast(ChannelHandler handler){
        if (handler == null){
            return;
        }
        DefaultChannelHandlerContext handlerContext = new DefaultChannelHandlerContext(handler, channel);
        AbstractChannelHandlerContext context = handlerContext;
        ((AbstractChannelHandlerContext)tail.prev()).next = context;
        context.prev = tail.prev();
        context.next = tail;
        ((AbstractChannelHandlerContext)tail).prev = context;
    }

    public ChannelHandlerContext getTail() {
        return tail;
    }

    public ChannelHandlerContext getHead() {
        return head;
    }

    class Tail implements ChannelHandler{

        @Override
        public void error(ChannelHandlerContext chc, Throwable e) throws IOException {
            chc.channel().close();
        }

        @Override
        public void read(ChannelHandlerContext chc, Object source) {
            if (log.isDebugEnabled()) {
                log.debug("tail 处理器, 读到数据: {}", source);
            }
            channel.getReadByteBuffer().clear();
        }

        @Override
        public void flush(ChannelHandlerContext chc) {
            chc.fireFlush(chc);
        }

        @Override
        public void write(ChannelHandlerContext chc, Object source) throws IOException {
            chc.fireWrite(chc, source);
        }
    }

    class Head implements ChannelHandler{


        @Override
        public void close(ChannelHandlerContext chc) {
            channel.getEventLoop().removeAndCloseChannel(channel);
        }

        @Override
        public void read(ChannelHandlerContext chc, Object source) throws IOException {
            chc.fireRead(chc, source);
        }

        @Override
        public void flush(ChannelHandlerContext chc) {
            if (log.isDebugEnabled()) {
                log.debug("head 处理器, flush");
            }
            AbstractNioChannel nioChannel = (AbstractNioChannel) channel;
            nioChannel.flush0();
        }

        @Override
        public void write(ChannelHandlerContext chc, Object source) {
            AbstractNioChannel nioChannel = (AbstractNioChannel) channel;
            byte[] buffer;
            if (source == null){
                //写入 null
                buffer = new byte[]{'n', 'u', 'l', 'l'};
            }else{
                buffer = IoUtils.getBytes(source, true);
            }
            if (log.isDebugEnabled()) {
                log.debug("head 处理器, 写数据: {}", buffer.length);
            }

            nioChannel.addBuffer(buffer);
        }
    }
}
