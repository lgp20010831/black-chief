package com.black.netty;

public interface NettySession extends Session{

    /**
     * Restart the server when you are a server,
     * and reconnect the client when you are a client
     */
    void restart();

    /**
     * Returns the global configuration class
     * that controls this service
     * @return config {@link Configuration}
     */
    Configuration getConfiguration();

}
