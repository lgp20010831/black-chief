package com.black.udp;

import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.util.Utils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class ClientDemo {


    public static void main(String[] args) throws IOException {
//        DatagramSocket socket = new DatagramSocket(6002);
//        while (true){
//            Utils.sleep(2000);
//            byte[] bytes = "HELLO".getBytes();
//            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), 6001);
//            socket.send(packet);
//            System.out.println("发送信息:" + Arrays.toString(bytes));
//        }
        UdpStableSocket socket = new UdpStableSocket(6002);
        socket.setCallback(new UdpCallback() {
            @Override
            public void read(UdpInputStream in, UdpSocket socket) throws Throwable {
                AsynGlobalExecutor.executeNoThrowable(() -> {
                    log.info("接到: " + in.getAddressString() + ", 发送消息: " + new String(in.readAll()));
                    Utils.sleep(1000);
                    //in.writeAndFlush("eee from 6002");
                });

            }
        });
        socket.start();
        socket.writeStable("hello 6001", 6001);
    }
}
