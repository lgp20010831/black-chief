package com.black.udp;

import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.utils.IoUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.*;

public class UdpSocket {

    @Setter @Getter
    protected IoLog log;

    protected final DatagramSocket socket;

    @Setter @Getter
    private UdpCallback callback;

    @Setter @Getter
    //接收缓冲区大小
    private int receiveBufferSize = 256;

    @Getter @Setter
    private boolean printError = true;

    private volatile boolean shutdown = false;

    private final UdpReadThread readThread;

    public static final String LOCAL_ADDRESS = "127.0.0.1";

    public UdpSocket(int port) throws IOException {
        this(InetAddress.getByName(LOCAL_ADDRESS), port);
    }

    public UdpSocket(InetAddress address, int port) throws IOException {
        this.socket = new DatagramSocket(port, address);
        log = new CommonLog4jLog();
        readThread = createThread();
    }

    protected UdpReadThread createThread(){
        return new UdpReadThread(this);
    }

    public void start(){
        readThread.start();
    }

    public boolean isShutdown(){
        return shutdown;
    }

    public void shutdown(){
        if (!isShutdown()){
            log.info("shutdown socket");
            socket.close();
            shutdown = true;
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void check() throws IOException {
        if (isShutdown()){
            throw new IOException("udpSocket is shutdown");
        }
    }

    public void write(Object source, int port) throws IOException {
        write(source, LOCAL_ADDRESS, port);
    }

    public void write(Object source, String host, int port) throws IOException {
        write(source, new InetSocketAddress(host, port));
    }

    public void write(Object source, SocketAddress address) throws IOException {
        check();
        byte[] bytes = IoUtils.getBytes(source, false);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address);
        try {
            socket.send(packet);
        }catch (IOException e){
            UdpCallback callback = getCallback();
            if (callback != null){
                try {
                    callback.sendIoThowable(e, this, address, bytes);
                } catch (Throwable ex) {
                    log.info("handle io throwable append new throwable: {}", IoUtils.getThrowableMessage(ex));
                }
            }else {
                throw e;
            }
        }
    }

}
