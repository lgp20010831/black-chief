package com.black.socket.server;

import com.black.socket.JHexSocketUtils;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.throwable.IOSException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

@Getter @Setter
public abstract class JHexServerSocket {

    private ServerSocket serverSocket;

    private String hostCache;

    private int portCache;

    private String address;

    private Thread bindThread;

    private IoLog log;

    private JHexSocketHandler socketHandler;

    private final LinkedBlockingQueue<ClientProcessor> clientProcessor = new LinkedBlockingQueue<>();

    public JHexServerSocket(int port){
        this("0.0.0.0", port);
    }

    public JHexServerSocket(String host, int port){
        hostCache = host;
        portCache = port;
        log = LogFactory.getArrayLog();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    protected abstract ClientProcessor createClientProcessor();

    public void bind(){
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(hostCache, portCache));
            address = hostCache + "|" + portCache;
            log.info("jHex server bind address: {}", address);
            bindThread = new JHexBindThreadHandler(this);
            bindThread.start();
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    public boolean isClosed(){
        return serverSocket == null || serverSocket.isClosed();
    }

    public void close(){
        if (serverSocket != null){
            log.info("jHex server socket closing....");
            for (ClientProcessor processor : clientProcessor) {
                processor.shutdown();
            }
            try {
                serverSocket.close();
            } catch (IOException e) {

            }
            serverSocket = null;
            bindThread = null;
        }
    }

    static class JHexBindThreadHandler extends Thread{

        private final JHexServerSocket jHexServerSocket;

        JHexBindThreadHandler(JHexServerSocket jHexServerSocket) {
            this.jHexServerSocket = jHexServerSocket;
        }

        @Override
        public void run() {
            while (!jHexServerSocket.isClosed()){
                ServerSocket serverSocket = this.jHexServerSocket.getServerSocket();
                IoLog log = jHexServerSocket.getLog();
                try {
                    Socket client = serverSocket.accept();
                    String address = JHexSocketUtils.getSocketAddress(client);
                    log.info("jHex server socket accept client -- {}", address);
                    LinkedBlockingQueue<ClientProcessor> queue = jHexServerSocket.getClientProcessor();
                    ClientProcessor target = null;
                    for (ClientProcessor processor : queue) {
                        if (processor.support(client)) {
                            target = processor;
                            break;
                        }
                    }
                    if (target == null){
                        target = jHexServerSocket.createClientProcessor();
                        queue.add(target);
                    }
                    target.registerClient(client);
                    log.info("jHex server socket register client -- {} to processor -- {}", address, target);
                } catch (IOException e) {
                    jHexServerSocket.close();
                }
            }
        }
    }
}
