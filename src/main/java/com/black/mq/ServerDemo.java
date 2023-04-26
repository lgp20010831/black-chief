package com.black.mq;

import com.black.mq.server.MQServer;
import com.black.mq.server.NettyMQServer;

import java.io.IOException;

public class ServerDemo {


    public static void main(String[] args) throws IOException {
        MQServer server = new NettyMQServer();
        server.bind("0.0.0.0", 9999);
    }

}
