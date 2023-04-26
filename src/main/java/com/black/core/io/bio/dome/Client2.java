package com.black.core.io.bio.dome;

import com.black.core.io.socket.SocketConfiguration;
import com.black.core.io.socket.SocketFactory;
import com.black.core.io.socket.SocketWrapper;

import java.io.IOException;

public class Client2 {

    public static void main(String[] args) throws IOException {
        SocketFactory factory = new SocketFactory(new SocketConfiguration(8888, "localhost"));
        SocketWrapper socketWrapper = factory.createSocketWrapper();
        socketWrapper.writeAndFlush("hello");
        socketWrapper.close();
    }

}
