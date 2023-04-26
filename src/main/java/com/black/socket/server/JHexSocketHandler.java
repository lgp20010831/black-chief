package com.black.socket.server;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.socket.JHexSocket;
import com.black.socket.JHexThrowableHandler;

public interface JHexSocketHandler extends JHexThrowableHandler {

    default void accept(JHexSocket socket, JHexServerSocket serverSocket){

    }

    void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket, JHexServerSocket serverSocket) throws Throwable;

}
