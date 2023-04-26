package com.black.core.io.bio.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RpcSocket {

    private final Socket socket;

    private DataOutputStream out;

    private DataInputStream in;

    public RpcSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public void writeInt(int i) throws IOException {
        out.writeInt(i);
    }

    public void write(String message) throws IOException {
        out.writeUTF(message);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public String read() throws IOException {
        return in.readUTF();
    }

    public void close() throws IOException {
        out.close();
        in.close();
    }
}
