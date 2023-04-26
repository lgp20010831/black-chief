package com.black.rpc.socket;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.nio.code.NioChannel;
import com.black.nio.code.NioSocketChannel;
import com.black.rpc.inter.RemoteSocket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class NioRemoteSocket implements RemoteSocket {
    private NioChannel nioChannel;

    public NioRemoteSocket(NioChannel nioChannel) {
        this.nioChannel = nioChannel;
    }

    @Override
    public String getNameAddress() {
        return nioChannel.nameAddress();
    }

    @Override
    public DataByteBufferArrayOutputStream getOutputStream() throws IOException {
        return nioChannel.getOutputStream();
    }

    @Override
    public boolean isConnected() throws IOException {
        SocketChannel channel = (SocketChannel) nioChannel.channel();
        return channel.isConnected();
    }

    @Override
    public void shutdown() {
        try {
            nioChannel.close();
        } catch (IOException e) {}
    }

    @Override
    public NioChannel getNioChannel() {
        return nioChannel;
    }

    @Override
    public void reconnect() {
        if (nioChannel instanceof NioSocketChannel){
            nioChannel = ((NioSocketChannel) nioChannel).reconnect();
        }else {
            throw new IllegalStateException("CURRENT CHANNEL IS SERVER CHANNEL");
        }
    }
}
