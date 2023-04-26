package com.black.core.io.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketFactory {

    public Socket newSocket(){
        return newSocket(5445);
    }

    public Socket newSocket(int port){
        return newSocket("localhost", port);
    }

    public Socket newSocket(String host, int port){
        return newSocket(host, port, 100);
    }

    public Socket newSocket(String host, int port, int linger){
        return newSocket(host, port, linger, 500);
    }

    public Socket newSocket(String host, int port, int linger, int connectTimeOut){
        return newSocket(host, port, linger, connectTimeOut, true);
    }

    public Socket newSocket(String host, int port, int linger, int connectTimeOut,
                            boolean noDelay){
        return newSocket(host, port, linger, connectTimeOut, noDelay, 0);
    }

    public Socket newSocket(String host, int port, int linger, int connectTimeOut,
                            boolean noDelay, int timeOut){
        return newSocket(host, port, linger, connectTimeOut, noDelay, timeOut, false);
    }

    public Socket newSocket(String host, int port, int linger, int connectTimeOut,
                            boolean noDelay, int timeOut, boolean keepAlive){
        Socket socket = new Socket();
        try {
            socket.setSoLinger(true, linger);
            socket.setTcpNoDelay(noDelay);
            socket.setKeepAlive(keepAlive);
            socket.setSoTimeout(timeOut);
            socket.connect(new InetSocketAddress(host, port), connectTimeOut);
        } catch (IOException e) {
            throw new BioSessionException(e);
        }
        return socket;
    }

    public ServerSocket newServerSocket(){
        return newServerSocket(5445);
    }

    public ServerSocket newServerSocket(int port){
        return newServerSocket("localhost", port);
    }

    public ServerSocket newServerSocket(String host, int port){
        return newServerSocket(host, port, -1);
    }

    public ServerSocket newServerSocket(String host, int port, int backlog){
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(host, port), backlog);
            return serverSocket;
        } catch (IOException e) {
            throw new BioSessionException(e);
        }
    }

}
