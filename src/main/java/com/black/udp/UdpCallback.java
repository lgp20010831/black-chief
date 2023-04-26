package com.black.udp;


import com.black.core.util.CentralizedExceptionHandling;

import java.io.IOException;
import java.net.SocketAddress;

public interface UdpCallback {


    void read(UdpInputStream in, UdpSocket socket) throws Throwable;

    default void sendIoThowable(IOException ex, UdpSocket socket, SocketAddress address, byte[] buffer) throws Throwable{
        if (socket.isPrintError())
        CentralizedExceptionHandling.handlerException(ex);
    }

    default void receThrowable(Throwable ex, UdpSocket socket, SocketAddress address) throws Throwable{
        if (socket.isPrintError())
        CentralizedExceptionHandling.handlerException(ex);
    }

    default void ioThrowable(IOException e, UdpSocket socket) throws IOException{
        socket.shutdown();
    }
}
