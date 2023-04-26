package com.black.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RpcServer {

    private static RpcWebServerApplicationContext serverApplicationContext;

    private RpcServer(){}

    public static RpcWebServerApplicationContext startServer(int port) throws IOException {
        return startServer("0.0.0.0", port);
    }

    public static RpcWebServerApplicationContext startServer(String host, int port) throws IOException {
        return startServer(new InetSocketAddress(host, port));
    }

    public static RpcWebServerApplicationContext startServer(InetSocketAddress address) throws IOException {
        if (serverApplicationContext == null){
            RpcConfiguration configuration = new RpcConfiguration();
            configuration.init();
            configuration.setPattern(Pattern.SERVER);
            configuration.setAddress(address);
            serverApplicationContext = new RpcWebServerApplicationContext(configuration);
        }
        return serverApplicationContext;
    }

    public static RpcWebServerApplicationContext getServerApplicationContext() {
        return serverApplicationContext;
    }
}
