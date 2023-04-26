package com.black.core.io.bio.dome;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {


    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 20; i++) {
            Socket socket = new Socket();
            socket.setKeepAlive(false);
            socket.setTcpNoDelay(true);
            new Thread(() ->{

                try {
                    socket.connect(new InetSocketAddress("localhost", 8888));
                    OutputStream stream = socket.getOutputStream();
                    for (int j = 0; j < 5; j++) {
                        Thread.sleep(3000);
                        stream.write(("hello" + j + "\n").getBytes("GBK"));
                        stream.flush();
                    }

                    socket.shutdownOutput();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        }

    }
}
