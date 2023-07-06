package com.black.compile;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.socket.JHexBytesHandler;
import com.black.socket.JHexSocket;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Scanner;

@SuppressWarnings("all") @Log4j2
public class client {


    public static void main(String[] args) throws IOException {
        JHexSocket socket = new JHexSocket(5000);
        socket.setJHexBytesHandler(new JHexBytesHandler() {
            @Override
            public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable {
                log.info("接到服务端数据: {}", in.readNewString());
            }
        });

        socket.connect();
        Scanner scanner = new Scanner(System.in);
        for (;;){
            String next = scanner.next();
            socket.writeAndFlush(next);
        }
    }
}
