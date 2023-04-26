package com.black.socket;

import java.io.IOException;

public class DEMO {


    public static void main(String[] args) throws IOException {
        DefaultOnerousServer server = new DefaultOnerousServer(2);
        server.setHandler((board, in) -> {
            String utf = in.readUnrestrictedUtf();
            System.out.println("接收到客户端消息: " + utf);
            board.prepare();
            board.writeAndFlushUtf("我是你爹");
        });
        server.bind("0.0.0.0", 4000);
    }
}
