package com.black.mq.server;

import com.black.io.in.JHexByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;

public class MQInputStream extends JHexByteArrayInputStream {

    private final String address;

    public MQInputStream(byte[] buffer, String address) {
        super(buffer);
        this.address = address;
    }

    public MQInputStream(InputStream in, String address) throws IOException {
        super(in);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

}
