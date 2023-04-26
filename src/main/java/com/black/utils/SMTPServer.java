package com.black.utils;

public enum SMTPServer {

    ALY("smtp.aliyun.com"),
    GG("smtp.gmail.com"),
    WY("smtp.163.com"),
    _126("smtp.126.com"),
    QQ("smtp.qq.com"),
    SH("smtp.sohu.com");

    String host;

    SMTPServer(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
