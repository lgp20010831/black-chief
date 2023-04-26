package com.black.netty;

public interface Session {

    /**
     * Writing data to the associated channel
     * will not immediately flush into the connecting party
     * @param message message
     */
    void write(String message);

    /**
     * Writing data to the associated channel
     * will immediately flush to the connecting party
     * @param message message
     */
    void writeAndFlush(String message);

    /**
     * close channel
     */
    void close();
}
