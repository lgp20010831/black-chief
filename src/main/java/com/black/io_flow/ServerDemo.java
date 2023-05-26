package com.black.io_flow;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.Factorys;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.nio.group.NioType;
import com.black.socket.JHexSocket;

import java.io.IOException;
import java.net.SocketException;

/**
 * @author 李桂鹏
 * @create 2023-05-25 11:07
 */
@SuppressWarnings("all")
public class ServerDemo {


    public static void main(String[] args) {
        Factorys.open(5000, new GioResolver() {
            @Override
            public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
                System.out.println("读到字节: " + bytes.length);
            }

            @Override
            public void acceptCompleted(GioContext context, JHexByteArrayOutputStream out) {
                JHexSocket source = (JHexSocket) context.source();
                try {
                    source.getSocket().setReceiveBufferSize(1000);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                GioResolver.super.acceptCompleted(context, out);
            }
        }, NioType.BIO).openSession();
    }

}
