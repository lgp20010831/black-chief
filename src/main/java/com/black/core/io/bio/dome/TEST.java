package com.black.core.io.bio.dome;

import com.black.core.io.bio.SocketFactory;

import java.io.IOException;
import java.net.Socket;

public class TEST {


    public static void main(String[] args) {
        SocketFactory socketFactory = new SocketFactory();
        new Thread(() ->{
            try {
                Socket socket = socketFactory.newServerSocket().accept();
                System.out.println("接受到客户端连接");
                for (;;){}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Socket socket = socketFactory.newSocket();
        System.out.println("连接成功");
    }
}
