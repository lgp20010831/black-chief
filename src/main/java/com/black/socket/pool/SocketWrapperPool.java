package com.black.socket.pool;

import java.io.IOException;
import java.net.Socket;

public class SocketWrapperPool extends SocketPool{

    public SocketWrapperPool(SocketPoolConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Socket createSocket() throws IOException {
        Socket socket = super.createSocket();
        return new SocketWrapper(socket);
    }

    public SocketWrapper getSocket(){
        Socket connection = getConnection();
        if (connection instanceof SocketWrapper){
            return (SocketWrapper) connection;
        }else {
            return new SocketWrapper(connection);
        }
    }
}
