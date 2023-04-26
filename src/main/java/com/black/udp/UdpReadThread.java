package com.black.udp;

import com.black.core.log.IoLog;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpReadThread extends Thread{

    public static final AtomicInteger num = new AtomicInteger(0);

    protected final UdpSocket socket;

    public UdpReadThread(UdpSocket socket){
        super("udp-io-" + num.incrementAndGet());
        this.socket = socket;
    }

    @Override
    public void run() {
        IoLog log = socket.getLog();
        DatagramSocket datagramSocket = this.socket.getSocket();

        log.info("read thread start up, listen address: {}|{}", datagramSocket.getLocalAddress(), datagramSocket.getPort());
        while (!this.socket.isShutdown()){
            byte[] buffer = new byte[this.socket.getReceiveBufferSize()];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {

                datagramSocket.receive(packet);
            } catch (IOException e) {
                UdpCallback callback = socket.getCallback();
                if (callback != null){
                    try {
                        callback.ioThrowable(e, socket);
                    } catch (IOException ex) {
                        //ignore
                    }
                }else {
                    socket.shutdown();
                }
                continue;
            }

            int length = packet.getLength();
            if (length == 0){
                continue;
            }
            UdpCallback callback = socket.getCallback();
            if (callback == null){
                continue;
            }

            byte[] receBytes = new byte[length];
            System.arraycopy(buffer, 0, receBytes, 0, receBytes.length);
            SocketAddress remoteAddress = packet.getSocketAddress();
            UdpInputStream inputStream = new UdpInputStream(receBytes, remoteAddress, socket);
            doResolveBytes(callback, inputStream);
        }
        log.info("socket shutdown, stop read thread");
    }

    protected void doResolveBytes(UdpCallback callback, UdpInputStream inputStream){
        IoLog log = socket.getLog();
        SocketAddress remoteAddress = inputStream.getAddress();
        try {
            callback.read(inputStream, socket);
        } catch (Throwable e) {
            try {
                callback.receThrowable(e, socket, remoteAddress);
            } catch (Throwable ex) {
                log.info("handle read throwable append new throwable: {}", IoUtils.getThrowableMessage(ex));
            }
        }
    }
}
