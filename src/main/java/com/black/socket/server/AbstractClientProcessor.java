package com.black.socket.server;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.socket.JHexBytesHandler;
import com.black.socket.JHexSocket;
import com.black.socket.JHexSocketUtils;
import com.black.core.log.IoLog;

import java.io.IOException;
import java.net.Socket;

public abstract class AbstractClientProcessor implements ClientProcessor{

    protected final JHexServerSocket serverSocket;

    protected final IoLog log;

    public AbstractClientProcessor(JHexServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        log = serverSocket.getLog();
    }

    public JHexServerSocket getServerSocket() {
        return serverSocket;
    }

    protected JHexSocket wrapperSocket(Socket socket)  {
        JHexSocketHandler socketHandler = serverSocket.getSocketHandler();
        JHexBytesHandler bytesHandler = new JHexBytesHandler() {
            @Override
            public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable {
                if (socketHandler != null){
                    socketHandler.resolveBytes(in, socket, serverSocket);
                }
            }

            @Override
            public void handlerIoEvent(IOException ex, JHexSocket socket) throws IOException {
                if (socketHandler != null){
                    socketHandler.handlerIoEvent(ex, socket);
                }
            }

            @Override
            public void handlerReadEvent(Throwable ex, JHexSocket socket) {
                if (socketHandler != null){
                    socketHandler.handlerReadEvent(ex, socket);
                }
            }
        };
        try {
            JHexSocket jHexSocket = new JHexSocket(socket, bytesHandler);
            jHexSocket.setHostCache(JHexSocketUtils.getRemoteHost(socket));
            jHexSocket.setPortCache(JHexSocketUtils.getRemotePort(socket));
            jHexSocket.setReconnectLoop(false);
            jHexSocket.setTryReconnect(false);
            jHexSocket.setCloseCallback(this::shutdown);
            if (socketHandler != null){
                socketHandler.accept(jHexSocket, serverSocket);
            }
            return jHexSocket;
        } catch (IOException e) {
            //ignore
            throw new IllegalStateException(e);
        }
    }
}
