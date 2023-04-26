package com.black.socket.server;

public class SingleServerSocket extends JHexServerSocket{
    public SingleServerSocket(int port) {
        super(port);
    }

    public SingleServerSocket(String host, int port) {
        super(host, port);
    }

    @Override
    protected ClientProcessor createClientProcessor() {
        return new SingleClientProcessor(this);
    }
}
