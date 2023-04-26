package com.black.core.io.bio;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

public interface Connection {

    void close();

    boolean isVaild();

    Socket getSocket();

    String read();

    String read(long timeout, TimeUnit unit);

    void write(String message);

    void writeAndFlush(String message);
}
