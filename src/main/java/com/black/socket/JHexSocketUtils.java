package com.black.socket;

import java.net.InetSocketAddress;
import java.net.Socket;

public class JHexSocketUtils {


    public static String getSocketAddress(Socket socket){
        if (socket.isClosed()) {
            return "socket[closed]";
        }

        if (!socket.isConnected()){
            return "socket[lost connection]";
        }

        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        return address.getHostString() + "|" + address.getPort();
    }

    public static String getRemoteHost(Socket socket){
        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        return address.getHostString();
    }

    public static int getRemotePort(Socket socket){
        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        return address.getPort();
    }

}
