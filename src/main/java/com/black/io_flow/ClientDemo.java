package com.black.io_flow;

import com.black.core.spring.util.ApplicationUtil;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.socket.JHexBytesHandler;
import com.black.socket.JHexSocket;
import com.black.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 李桂鹏
 * @create 2023-05-25 11:07
 */
@SuppressWarnings("all")
public class ClientDemo {

    public static void main(String[] args) throws IOException {
        JHexSocket socket = new JHexSocket(1162);
        socket.setJHexBytesHandler(new JHexBytesHandler() {
            @Override
            public void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable {
                System.out.println(in.readNewString());
            }
        });
        socket.connect();
        InputStream inputStream = ServiceUtils.getNonNullResource("open-chief-sdk.jar");
        ApplicationUtil.programRunMills(() -> {
            try {
                socket.writeAndFlush(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
