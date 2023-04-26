package com.black.socket;

import java.io.IOException;

public interface JHexThrowableHandler {


    default void handlerReadEvent(Throwable ex, JHexSocket socket){

    }

    default void handlerIoEvent(IOException ex, JHexSocket socket) throws IOException {
        if (socket.isTryReconnect()){
            socket.reconnect();
        }
    }


}
