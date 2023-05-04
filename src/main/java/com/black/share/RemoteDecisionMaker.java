package com.black.share;

import lombok.Data;

import java.io.IOException;
import java.net.Socket;

@Data @SuppressWarnings("all")
public abstract class RemoteDecisionMaker implements MethodInvoker{

    private String host = "0.0.0.0";

    private int port = 12345;

    private ShareServer shareServer;

    private ShareClient shareClient;

    public RemoteDecisionMaker(){
        run();
    }

    public RemoteDecisionMaker(int port) {
        this.port = port;
        run();
    }

    public RemoteDecisionMaker(String host, int port) {
        this.host = host;
        this.port = port;
        run();
    }

    public void shutdown(){
        if (shareServer != null){
            shareServer.shutdown();
        }
    }

    public Object invokeMethodCastThrowable(String name, Object... args){
        try {
            return invokeMethod(name, args);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object invokeMethod(String name, Object... args) throws Throwable {
        return shareServer == null ? shareClient.invokeMethod(name, args) : shareServer.invokeMethod(name, args);
    }

    public void run(){
        if (tryCognitionServer()) {
            shareServer = openServer(host, port);
        }else {
            shareClient = openClient(host, port);
        }
    }

    protected abstract ShareServer openServer(String host, int port);

    protected abstract ShareClient openClient(String host, int port);

    private boolean tryCognitionServer(){
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            return true;
        }finally {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
        return false;
    }
}
