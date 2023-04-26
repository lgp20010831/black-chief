package com.black.rpc.inter;

import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.nio.code.NioChannel;

import java.io.IOException;

public interface RemoteSocket {

    String getNameAddress();

    DataByteBufferArrayOutputStream getOutputStream() throws IOException;

    boolean isConnected() throws IOException;

    void shutdown();

    NioChannel getNioChannel();

    void reconnect();
}
