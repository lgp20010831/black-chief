package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;

public interface SocketHandler {

    void read(SocketBoard board, JHexByteArrayInputStream in) throws Throwable;

    default void complete(SocketBoard board) throws Throwable{

    }

    default void close(SocketBoard board, Throwable ex){

    }

}
