package com.black.mq.server;

import com.black.io.out.JHexByteArrayOutputStream;

import java.io.OutputStream;
import java.util.Objects;

public class MQOutputStream extends JHexByteArrayOutputStream {

    private final String address;

    public MQOutputStream(String clientAddress) {
        address = clientAddress;
    }

    public MQOutputStream(OutputStream writeOut, String clientAddress) {
        super(writeOut);
        address = clientAddress;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MQOutputStream that = (MQOutputStream) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
