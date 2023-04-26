package com.black.core.io.bio;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.LockSupport;

public class BioUtils {

    public static void park(){
        LockSupport.park();
    }

    public static void unpark(Thread t){
        LockSupport.unpark(t);
    }

    public static void closeSocket(Socket socket) throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public static void discardSocket(Socket socket){

    }
}
