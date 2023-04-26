package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.io.bio.BioSessionException;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Server {

    private final ServerSocket serverSocket;

    private ThreadPoolExecutor workPool;

    protected SocketHandler handler;

    protected IoLog log;

    public Server() {
        try {
            serverSocket = new ServerSocket();
        } catch (IOException e) {
            throw new BioSessionException(e);
        }
        log = LogFactory.getArrayLog();
    }

    public IoLog getLog() {
        return log;
    }

    public void setHandler(SocketHandler handler) {
        this.handler = handler;
    }

    public SocketHandler getHandler() {
        return handler;
    }

    public void setWorkPool(ThreadPoolExecutor workPool) {
        this.workPool = workPool;
    }

    public void setLog(IoLog log) {
        this.log = log;
    }

    public ThreadPoolExecutor getWorkPool() {
        if (workPool == null){
            workPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        }
        return workPool;
    }

    public void bind(String host, int port) throws IOException {
        doBind(host, port);
    }

    private void doBind(String host, int port) throws IOException {
        serverSocket.bind(new InetSocketAddress(host, port));
        log.info("server bind --> host: [{}] -- port: [{}]", host, port);
        while (true){
            try {
                Socket client = serverSocket.accept();
                acceptClient(client);
            }catch (Throwable e){
                log.error(e, "accept client error: [{}]", e.getMessage());
            }
        }
    }

    protected void closeClient(SocketBoard board, Throwable e){
        log.error(null, "close client: [{}]", board.getAddress());
        if (handler != null){
            board.close();
            handler.close(board, e);
        }
    }

    protected abstract void acceptClient(Socket socket) throws IOException;

    protected void execute(SocketBoard soh, JHexByteArrayInputStream dataIn) throws IOException {
        int type = dataIn.readInt();
        if (type == 9999){
            //心跳包处理
            log.debug("检测到心跳包发送: {}", soh.getAddress());
            soh.ping();
        }else if (type == 200){
            //数据包处理
            log.debug("处理数据包");
            ServerReadHandler readHandler = new ServerReadHandler(dataIn, soh, handler, this);
            getWorkPool().execute(readHandler);
        }else {
            log.debug("Unrecognized packet, type: {}", type);
        }
    }

    protected static class ServerReadHandler implements Runnable{

        private final JHexByteArrayInputStream dataIn;

        private final SocketBoard soh;

        private final SocketHandler handler;

        private final Server server;

        public ServerReadHandler(JHexByteArrayInputStream dataIn, SocketBoard soh, SocketHandler handler, Server server) {
            this.dataIn = dataIn;
            this.soh = soh;
            this.handler = handler;
            this.server = server;
        }

        @Override
        public void run() {
            if (handler != null){
                try {

                    handler.read(soh, dataIn);
                } catch (Throwable e) {
                    server.closeClient(soh, e);
                }
            }
        }
    }
}
