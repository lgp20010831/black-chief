package com.black.netty;

import java.net.SocketAddress;

public interface IoImpl {

    /**
     * Returns the configured address
     * @return address
     */
    SocketAddress getAddress();

    /**
     * Judge whether the type of the current
     * implementation class is a server
     * @return is server as true
     */
    boolean isServer();

    /**
     * When the implementation class is a server,
     * bind the specified address and start the service
     * @return netty channel
     */
    NettySession bind();

    /**
     * When the implementation class is a client,
     * call to connect to the server at the specified address
     * @return netty channel
     */
    NettySession connect();
}
