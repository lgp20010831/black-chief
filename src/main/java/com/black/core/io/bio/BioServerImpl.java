package com.black.core.io.bio;

import com.black.netty.ill.SessionException;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class BioServerImpl {

    private final Configuration configuration;
    private final SocketAddress address;
    private final ThreadPoolExecutor ioPool;
    private final ThreadPoolExecutor workPool;
    private final LinkedBlockingQueue<BioClientReader> clientReaderQueue = new LinkedBlockingQueue<>();
    private final ArrayList<BioClientReader> readerArray = new ArrayList<>();

    public BioServerImpl(Configuration configuration) {
        this.configuration = configuration;
        final String ip = configuration.getIp();
        final int port = configuration.getPort();
        if (port < 0 || port > 65535){
            throw new SessionException("port should is 0 - 65535");
        }
        if (ip == null){
            address = new InetSocketAddress(port);
        }else {
            address = new InetSocketAddress(ip, port);
        }
        int coreSize = configuration.getCoreSize();
        if (coreSize <= 0 || coreSize >= 20){
            coreSize = 5;
        }
        ioPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(coreSize, new ReaderThreadFactory());
        workPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(configuration.getWorkCoreSize(), new WorkThreadFactory());
        BioClientReader headClientReader = new BioClientReader(configuration, this, configuration.getAlive(), 0);
        clientReaderQueue.add(headClientReader);
        readerArray.add(headClientReader);

    }

    public void bind(){
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(address);
            if (log.isInfoEnabled()) {
                log.info("服务端启动完成, 监听地址: {}", serverSocket.getInetAddress());
            }
            for (BioClientReader bioClientReader : clientReaderQueue) {
                ioPool.execute(bioClientReader);
            }
            ioPool.execute(() ->{
                for (;;){
                    try {
                        Socket client = serverSocket.accept();
                        if (log.isInfoEnabled()) {
                            log.info("监听到客户端接入: {}", client.getRemoteSocketAddress());
                        }
                        BioClientReader clientReader = clientReaderQueue.peek();
                        clientReader.add(client);
                    } catch (IOException e) {
                        CentralizedExceptionHandling.handlerException(e);
                    }
                }
            });
        }catch (Throwable e){
            throw new BioSessionException(e);
        }
    }

    public void transferReader(BioClientReader reader, Socket client) throws SocketException {
        if (client.isClosed()) {
            return;
        }
        int index = reader.getIndex();
        int alive = reader.getAlive();
        int size = clientReaderQueue.size();
        if (index >= size -1){
            synchronized (clientReaderQueue){
                log.info("创建新的 client reader");
                BioClientReader newClientReader= new BioClientReader(configuration, this, alive * 2, index + 1);
                newClientReader.add(client);
                clientReaderQueue.add(newClientReader);
                readerArray.add(newClientReader);
                ioPool.execute(newClientReader);
            }
        }else {

            log.info("添加到 client reader, 下标: {}", index + 1);
            BioClientReader nextClientReader = readerArray.get(index + 1);
            nextClientReader.add(client);
        }
    }

    public void submitMessageTask(BioClientReader reader, String data, Socket socket){
        workPool.execute(() ->{
            log.info("接收到客户端: {} 发送的消息: {}", socket.getInetAddress(), data);
        });
    }

}
