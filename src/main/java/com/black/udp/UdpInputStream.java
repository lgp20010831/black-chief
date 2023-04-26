package com.black.udp;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.utils.IoUtils;
import lombok.NonNull;

import java.io.IOException;
import java.net.SocketAddress;

public class UdpInputStream extends JHexByteArrayInputStream {

    private final SocketAddress address;

    private final String addressString;

    private final UdpSocket udpSocket;

    public UdpInputStream(byte[] buffer, @NonNull SocketAddress address, UdpSocket udpSocket) {
        super(buffer);
        this.address = address;
        addressString = address.toString();
        this.udpSocket = udpSocket;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getAddressString() {
        return addressString;
    }

    public UdpOutputStream getOutputStream(){
        return new UdpOutputStream(address, udpSocket);
    }

    public void writeAndFlush(Object source) throws IOException {
        writeAndFlush(source, true);
    }

    public void writeAndFlush(Object source, boolean serialize) throws IOException {
        byte[] bytes = IoUtils.getBytes(source, serialize);
        UdpOutputStream stream = getOutputStream();
        stream.write(bytes);
        stream.flush();
        stream.close();
    }
}
