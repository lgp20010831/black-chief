package com.black.http;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Log4j2
public abstract class AbstractSocketTransitExecutor implements HttpTransitExecutor{

    protected abstract Socket createSocket(Configuration configuration) throws IOException;

    @Override
    public byte[] send(byte[] request, Configuration configuration) throws IOException {
        Socket socket = createSocket(configuration);
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request);
            outputStream.flush();
            InputStream inputStream = socket.getInputStream();
            log.info("等待中转服务器响应");
            return SocketUtils.read0(inputStream, socket).readAll();
        }finally {
            socket.close();
        }
    }
}
