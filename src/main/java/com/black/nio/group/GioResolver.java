package com.black.nio.group;

import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;

public interface GioResolver {

    //当客户端连接完成时触发
    default void connectCompleted(GioContext context){

    }

    //当作为服务端接收到连接后触发
    default void acceptCompleted(GioContext context, JHexByteArrayOutputStream out){


    }

    //每次触发写事件时执行
    default void write(GioContext context, Object source){

    }

    void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException;


    //发生异常时
    default void trowable(GioContext context, Throwable ex, JHexByteArrayOutputStream out){

    }

    //连接关闭时
    default void close(GioContext context){

    }
}
