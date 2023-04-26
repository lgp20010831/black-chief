package com.black.nio.code;

import java.io.IOException;

public interface ChannelHandler {

    //当有连接建立时触发
    default void active(ChannelHandlerContext chc)      {chc.fireActive(chc);}

    //将数据刷出去
    default void flush(ChannelHandlerContext chc)       {chc.fireFlush(chc);}

    //主要方法
    void read(ChannelHandlerContext chc, Object source) throws IOException;

    //连接完成
    default void connectComplete(ChannelHandlerContext chc){
        chc.fireConnectComplete(chc);
    }

    //接受连接完成
    default void acceptComplete(ChannelHandlerContext chc){
        chc.fireAcceptComplete(chc);
    }

    //当有异常流入时触发异常
    default void error(ChannelHandlerContext chc, Throwable e)
            throws IOException                          {chc.fireError(chc, e);}

    //每次 write 数据时触发事件
    default void write(ChannelHandlerContext chc, Object source) throws IOException {chc.fireWrite(chc, source);}
    //当 channel 关闭时触发该事件
    //该 channel 可能是任意一个客户端或者服务端本身
    default void close(ChannelHandlerContext chc)       {chc.fireClose(chc);}
}
