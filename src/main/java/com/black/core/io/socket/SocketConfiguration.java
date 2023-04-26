package com.black.core.io.socket;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
public class SocketConfiguration {

    private final int port;
    private final String host;
    private int timeOut;
    private int soTimeOut;
    private Charset charset = StandardCharsets.UTF_8;

    public SocketConfiguration(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void setSoTimeOut(int soTimeOut) {
        this.soTimeOut = soTimeOut;
    }
}
