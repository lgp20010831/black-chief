package com.black.core.io.bio;

import com.black.core.util.CentralizedExceptionHandling;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all") @Getter  @Log4j2
public class BioClientReader implements Runnable{

    private final LinkedBlockingQueue<Socket> clientQueue = new LinkedBlockingQueue<>();
    private final Configuration configuration;
    private final BioServerImpl server;
    private final int alive;
    private Thread currentThread;
    private volatile boolean park = false;
    private final int index;
    private String name;

    public BioClientReader(Configuration configuration, BioServerImpl server, int alive, int index) {
        this.configuration = configuration;
        this.server = server;
        this.alive = alive;
        this.index = index;
    }

    public void add(Socket client) throws SocketException {
        if (client != null){
            client.setSoTimeout(alive);
            clientQueue.add(client);
            if (park){

                //唤醒线程
                if (log.isInfoEnabled()) {
                    log.info("唤醒 clientReader -- {}", name);
                }
                BioUtils.unpark(currentThread);
                park = false;
            }
        }
    }

    @Override
    public void run() {
        currentThread = Thread.currentThread();
        name = currentThread.getName() + "-impl";

        if (log.isInfoEnabled()) {
            log.info("{} running ...", name);
        }
        for (;;){

            //如果没有要处理的客户端
            //则将线程挂起
            if (clientQueue.isEmpty()){
                park = true;
                if (log.isInfoEnabled()) {
                    log.info("挂起  clientReader -- {}", name);
                }
                BioUtils.park();
                if (log.isInfoEnabled()) {
                    log.info("clientReader -- {} -- 激活成功", name);
                }
            }

            Iterator<Socket> clientIterator = clientQueue.iterator();
            while (clientIterator.hasNext()) {
                Socket client = clientIterator.next();
                if (client.isClosed()) {

                    if (log.isInfoEnabled()) {
                        log.info("丢弃 client: {}", client.getRemoteSocketAddress());
                    }
                    clientIterator.remove();
                    continue;
                }
                BufferedReader reader = null;
                StringBuffer stringBuffer = new StringBuffer();
                try {

                    reader = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                    String line;
                    while (StringUtils.hasText(line = reader.readLine())){
                        stringBuffer.append(line);
                    }

                    //read data
                    server.submitMessageTask(this, stringBuffer.toString(), client);
                    reader.close();
                } catch (IOException e) {
                    if (e instanceof SocketTimeoutException){
                        //join client to other reader
                        clientIterator.remove();
                        try {
                            if (!client.isClosed()) {

                                //将此客户端转移下一个 reader
                                log.info("委托客户端: {}", client.getRemoteSocketAddress());
                                server.transferReader(this, client);
                            }else {

                                log.info("关闭客户端: {}", client.getRemoteSocketAddress());
                                BioUtils.closeSocket(client);
                            }

                        } catch (SocketException ex) {
                            e = ex;
                        } catch (IOException ioException) {
                            CentralizedExceptionHandling.handlerException(ioException);
                        }
                    }
                    continue;
                }
            }
        }
    }
}
