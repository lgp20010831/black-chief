package com.black.rpc.demo;

import com.black.rpc.RpcServer;
import com.black.rpc.RpcWebServerApplicationContext;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {
        RpcWebServerApplicationContext context = RpcServer.startServer(4000);
        context.scanAction("com.example.rpc.demo");
    }
}
