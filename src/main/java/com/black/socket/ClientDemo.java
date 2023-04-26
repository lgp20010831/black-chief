package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.socket.pool.SocketPoolConfiguration;
import com.black.socket.pool.SocketWrapper;
import com.black.socket.pool.SocketWrapperPool;
import com.black.core.util.Utils;

import java.io.IOException;

public class ClientDemo {


    public static void main(String[] args) throws IOException {
        SocketPoolConfiguration configuration = new SocketPoolConfiguration(4000);
        SocketWrapperPool pool = new SocketWrapperPool(configuration);
        for (;;){
            SocketWrapper connection = pool.getSocket();
            connection.writeAndFlush("hhhh woc");
            JHexByteArrayInputStream inputStream = connection.waitRead();
            System.out.println(inputStream.readNewString());
            Utils.sleep(2000);
        }

    }
}
