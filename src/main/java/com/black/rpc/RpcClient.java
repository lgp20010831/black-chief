package com.black.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RpcClient {

    private static RpcWebClientApplicationContext clientApplicationContext;

    public static RpcWebClientApplicationContext startClient(int port) throws IOException {
        return startClient("0.0.0.0", port);
    }

    public static RpcWebClientApplicationContext startClient(String host, int port) throws IOException {
        return startClient(new InetSocketAddress(host, port));
    }

    public static RpcWebClientApplicationContext startClient(InetSocketAddress address) throws IOException {
        if (clientApplicationContext == null){
            RpcConfiguration configuration = new RpcConfiguration();
            configuration.init();
            configuration.setPattern(Pattern.CLIENT);
            configuration.setAddress(address);
            clientApplicationContext = new RpcWebClientApplicationContext(configuration);
        }
        return clientApplicationContext;
    }

}
