package com.black.core.io.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketFactory {

    private final SocketConfiguration configuration;

    public SocketFactory(SocketConfiguration configuration) {
        this.configuration = configuration;
    }

    public SocketWrapper createSocketWrapper(){
        return new SocketWrapper(createSocket(), configuration);
    }

    public Socket createSocket(){
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(configuration.getHost(), configuration.getPort()), configuration.getTimeOut());
            socket.setSoTimeout(configuration.getSoTimeOut());
        }catch (Throwable e){
            try {
                socket.close();
            } catch (IOException ex) {
                throw new SocketFactoryException(ex);
            }
            throw new SocketFactoryException(e);
        }
        return socket;
    }
}
