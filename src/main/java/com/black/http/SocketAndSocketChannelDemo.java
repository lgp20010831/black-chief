package com.black.http;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.Factorys;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.nio.group.NioType;
import com.black.socket.AvailableSocketPolling;
import com.black.socket.JHexBytesHandler;
import com.black.socket.JHexSocket;
import com.black.core.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketAndSocketChannelDemo {

    public static void main(String[] args) throws IOException {
        startServerSocketChannel();
        Utils.sleep(5000);
        startSocket();
        //createJhex();
    }

    static void startSocket() throws IOException {
        String msg = "hello";
        Socket socket = new Socket("0.0.0.0", 3333);
        socket.setSoTimeout(3000);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(msg.getBytes());
        outputStream.flush();

        InputStream inputStream = socket.getInputStream();
        System.out.println("等待服务器响应");
//        byte[] buf = new byte[1024];
//        int n = 0;
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        while (-1 != (n = inputStream .read(buf))) {
//            output.write(buf, 0, n);
//        }
        byte[] rpcBytes = AvailableSocketPolling.read0(inputStream, socket).readAll();
        System.out.println(new String(rpcBytes));
        socket.close();
    }

    static void createJhex() throws IOException {
        String msg = "hello";
        JHexSocket socket = new JHexSocket(3333);
        socket.setJHexBytesHandler(new JHexBytesHandler() {
            @Override
            public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable {
                System.out.println(new String(in.readAll()));
            }
        });
        socket.connect();
        socket.writeAndFlush(msg);
    }

    static void startServerSocketChannel() throws IOException {
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//        serverSocketChannel.configureBlocking(false);
//        Selector selector = Selector.open();
//        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        Factorys.open(new GioResolver() {
            @Override
            public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
                System.out.println(new String(bytes));
                context.writeAndFlush("sb");
            }
        }, NioType.CHIEF).openSession();

    }


}
