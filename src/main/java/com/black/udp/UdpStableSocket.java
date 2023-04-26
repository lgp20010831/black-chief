package com.black.udp;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.core.log.IoLog;
import com.black.core.util.Utils;
import com.black.utils.IoUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpStableSocket extends UdpSocket{

    private final Map<SocketAddress, Map<String, StablePackage>> packageCache = new ConcurrentHashMap<>();

    public static final int STABLE_FLAG = 9999;

    public static final int CONFIRM_FLAG = 7777;

    @Getter @Setter
    private long loopTime = 5000;

    private ReSendPackageThread reSendPackageThread;

    public UdpStableSocket(int port) throws IOException {
        super(port);
        reSendPackageThread = new ReSendPackageThread(this);
    }

    public UdpStableSocket(InetAddress address, int port) throws IOException {
        super(address, port);
        reSendPackageThread = new ReSendPackageThread(this);
    }

    public Map<SocketAddress, Map<String, StablePackage>> getPackageCache() {
        return packageCache;
    }

    @Override
    protected UdpReadThread createThread() {
        return new UdpStableReadThread(this);
    }

    public void removePackage(SocketAddress address, String id){
        Map<String, StablePackage> map = packageCache.computeIfAbsent(address, ad -> new ConcurrentHashMap<>());
        map.remove(id);
    }

    @Override
    public void start() {
        super.start();
        reSendPackageThread.start();
    }

    public void writeStable(Object source, int port) throws IOException {
        writeStable(source, LOCAL_ADDRESS, port);
    }

    public void writeStable(Object source, String host, int port) throws IOException {
        writeStable(source, new InetSocketAddress(host, port));
    }

    public void writeStable(Object source, SocketAddress address) throws IOException {
        byte[] bytes = IoUtils.getBytes(source, false);
        StablePackage stablePackage = new StablePackage(bytes);
        JHexByteArrayOutputStream outputStream = new JHexByteArrayOutputStream();
        outputStream.writeInt(STABLE_FLAG);
        outputStream.writeHexJavaObject(stablePackage);
        write(outputStream.toByteArray(), address);
        Map<SocketAddress, Map<String, StablePackage>> packageCache = getPackageCache();
        Map<String, StablePackage> map = packageCache.computeIfAbsent(address, ad -> new ConcurrentHashMap<>());
        map.put(stablePackage.getId(), stablePackage);
        log.info("send stable package :{} to address:{}", stablePackage.getId(), address);
    }

    @Override
    public void write(Object source, int port) throws IOException {
        super.write(source, port);
    }

    @Override
    public void write(Object source, String host, int port) throws IOException {
        super.write(source, host, port);
    }

    @Override
    public void write(Object source, SocketAddress address) throws IOException {
        super.write(source, address);
    }

    public void sendConfirmPackage(String id, SocketAddress address) throws IOException {
        IoLog log = getLog();
        UdpConfirmPackage confirmPackage = new UdpConfirmPackage(id);
        JHexByteArrayOutputStream outputStream = new JHexByteArrayOutputStream();
        outputStream.writeInt(CONFIRM_FLAG);
        outputStream.writeHexJavaObject(confirmPackage);
        write(outputStream.toByteArray(), address);
        log.info("send confirm package: {} to {}", id, address);
    }

    public static class ReSendPackageThread extends Thread{

        private static final AtomicInteger num = new AtomicInteger(0);

        private final UdpStableSocket socket;

        public ReSendPackageThread(UdpStableSocket socket){
            super("udp-resend-" + num.incrementAndGet());
            this.socket = socket;
        }

        @Override
        public void run() {
            IoLog log = socket.getLog();
            DatagramSocket datagramSocket = this.socket.getSocket();
            log.info("stable thread start up on {}|{}", datagramSocket.getLocalAddress(), datagramSocket.getPort());
            while (!this.socket.isShutdown()){
                long loopTime = this.socket.getLoopTime();
                Utils.sleep(loopTime);
                Map<SocketAddress, Map<String, StablePackage>> packageCache = this.socket.getPackageCache();
                for (SocketAddress address : packageCache.keySet()) {
                    Map<String, StablePackage> packageMap = packageCache.get(address);
                    for (StablePackage pack : packageMap.values()) {
                        if (System.currentTimeMillis() - pack.getSavePoint() >= loopTime){
                            log.info("resend ===> {} pack: {}", address, pack.getId());
                            byte[] bytes = pack.getBytes();
                            try {
                                socket.writeStable(bytes, address);
                            } catch (IOException e) {
                                UdpCallback callback = socket.getCallback();
                                if (callback != null){
                                    try {
                                        callback.sendIoThowable(e, socket, address, bytes);
                                    } catch (Throwable ex) {
                                        log.info("handle io throwable append new throwable: {}", IoUtils.getThrowableMessage(ex));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            log.info("stable thread shutdown");
        }
    }
}
