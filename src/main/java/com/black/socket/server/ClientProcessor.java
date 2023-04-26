package com.black.socket.server;

import java.net.Socket;

public interface ClientProcessor {

    boolean support(Socket socket);

    void registerClient(Socket socket);

    void  shutdown();
}
