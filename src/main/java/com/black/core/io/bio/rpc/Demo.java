package com.black.core.io.bio.rpc;

import java.io.IOException;
import java.util.UUID;

public class Demo {

    public static void main(String[] args) throws IOException {
        RpcSocket socket = new RpcSocket("127.0.0.1", 4000);
        socket.writeInt(1);
        socket.write(UUID.randomUUID().toString() + "userList^driver");
        socket.flush();
        System.out.println(socket.read());
        socket.close();
    }
}
