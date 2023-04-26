package com.black.nio.code;

import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;

public interface ChannelHandlerContext {

    default void flush(){
        channel().flush();
    }

    default void write(Object source){
        channel().write(source);
    }

    default void writeAndFlush(Object source){
        channel().writeAndFlush(source);
    }

    default void close() throws IOException {
        channel().close();
    }

    default String nameAddress(){
        return channel().nameAddress();
    }

    default Configuration getConfiguration(){
        return channel().getConfiguration();
    }

    ChannelHandlerContext next();

    ChannelHandlerContext prev();

    NioChannel channel();

    default JHexByteArrayOutputStream getOutputStream(){
        return channel().getOutputStream();
    }

    void fireActive(ChannelHandlerContext chc);

    void fireError(ChannelHandlerContext chc, Throwable ex) throws IOException;

    void fireClose(ChannelHandlerContext chc);

    void fireFlush(ChannelHandlerContext chc);

    void fireRead(ChannelHandlerContext chc, Object source) throws IOException;

    void fireWrite(ChannelHandlerContext chc, Object source) throws IOException;

    void fireConnectComplete(ChannelHandlerContext chc);

    void fireAcceptComplete(ChannelHandlerContext chc);

    ChannelHandler handler();
}
