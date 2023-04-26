package com.black.udp;

import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;
import java.net.SocketAddress;

public class UdpOutputStream extends JHexByteArrayOutputStream {

    private final SocketAddress address;

    private final UdpSocket socket;

    public UdpOutputStream(SocketAddress address, UdpSocket socket) {
        this.address = address;
        this.socket = socket;
    }

    @Override
    public void flush() throws IOException {
        byte[] bytes = toByteArray();
        socket.write(bytes, address);
        reset();
    }
}
