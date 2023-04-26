package com.black.udp;

import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.util.Utils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class ServerDemo {


    public static void main(String[] args) throws IOException {
//        DatagramSocket socket = new DatagramSocket(6001);
//        while (true){
//            byte[] bytes = new byte[256];
//            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
//            socket.receive(packet);
//            System.out.println("接收到信息:" + new String(packet.getData(), 0, packet.getLength()));
//        }
        UdpSocket socket = new UdpStableSocket(6001);
        socket.setCallback(new UdpCallback() {
            @Override
            public void read(UdpInputStream in, UdpSocket socket) throws Throwable {
                AsynGlobalExecutor.executeNoThrowable(() -> {
                    log.info("接到: " + in.getAddressString() + ", 发送消息: " + new String(in.readAll()));
                    Utils.sleep(1000);
                    //in.writeAndFlush("eee from 6001");
                });
            }
        });
        socket.start();
    }
}
