package com.black.socket.server;

import com.black.socket.JHexSocket;

import java.io.IOException;
import java.net.Socket;

@SuppressWarnings("all")
public class SingleClientProcessor extends AbstractClientProcessor{

    private JHexSocket jHexSocket;

    public SingleClientProcessor(JHexServerSocket serverSocket) {
        super(serverSocket);
    }

    @Override
    public boolean support(Socket socket) {
        return this.jHexSocket == null;
    }

    @Override
    public void registerClient(Socket socket) {
        jHexSocket = wrapperSocket(socket);
    }

    @Override
    public void shutdown() {
        if (jHexSocket != null){
            log.info("shutdown singlClient -- {}", jHexSocket.getServerName());
            try {
                jHexSocket.close();
            } catch (IOException e) {}
            jHexSocket = null;
        }
    }
}
