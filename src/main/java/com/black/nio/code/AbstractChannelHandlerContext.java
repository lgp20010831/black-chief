package com.black.nio.code;

import java.io.IOException;

public abstract class AbstractChannelHandlerContext implements ChannelHandlerContext{

     ChannelHandlerContext next;
     ChannelHandlerContext prev;
    protected final ChannelHandler handler;

    protected final NioChannel channel;

    public AbstractChannelHandlerContext(ChannelHandler handler, NioChannel channel) {
        this.handler = handler;
        this.channel = channel;
    }

    @Override
    public NioChannel channel() {
        return channel;
    }

    @Override
    public void fireError(ChannelHandlerContext chc, Throwable ex) throws IOException {
        ChannelHandlerContext next = chc.next();
        if (next != null){
            next.handler().error(next, ex);
        }
    }

    @Override
    public void fireActive(ChannelHandlerContext chc) {
        ChannelHandlerContext next = chc.next();
        if (next != null){
            next.handler().active(next);
        }
    }

    @Override
    public void fireAcceptComplete(ChannelHandlerContext chc) {
        ChannelHandlerContext next = chc.next();
        if (next != null){
            next.handler().acceptComplete(next);
        }
    }

    @Override
    public void fireConnectComplete(ChannelHandlerContext chc) {
        ChannelHandlerContext next = chc.next();
        if (next != null){
            next.handler().connectComplete(next);
        }
    }

    @Override
    public void fireRead(ChannelHandlerContext chc, Object source) throws IOException {
        ChannelHandlerContext next = chc.next();
        if (next != null){
            next.handler().read(next, source);
        }
    }

    @Override
    public void fireWrite(ChannelHandlerContext chc, Object source) throws IOException{
        ChannelHandlerContext prev = chc.prev();
        if (prev != null){
            prev.handler().write(prev, source);
        }
    }

    @Override
    public void fireClose(ChannelHandlerContext chc) {
        ChannelHandlerContext prev = chc.prev();
        if (prev != null){
            prev.handler().close(prev);
        }
    }

    @Override
    public void fireFlush(ChannelHandlerContext chc) {
        ChannelHandlerContext prev = chc.prev();
        if (prev != null){
            prev.handler().flush(prev);
        }
    }

    @Override
    public ChannelHandler handler() {
        return handler;
    }

    @Override
    public ChannelHandlerContext next() {
        return next;
    }

    @Override
    public ChannelHandlerContext prev() {
        return prev;
    }
}
