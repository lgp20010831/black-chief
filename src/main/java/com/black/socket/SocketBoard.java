package com.black.socket;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.io.out.DataByteBufferArrayOutputStream;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

@Getter
public class SocketBoard {

    private long lastPingTime;

    private final Socket socket;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final DataByteBufferArrayOutputStream dataOutput;

    private final DataByteBufferArrayInputStream dataInput;

    private InetAddress address;

    public SocketBoard(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        address = socket.getInetAddress();
        lastPingTime = System.currentTimeMillis();
        dataOutput = new DataByteBufferArrayOutputStream(outputStream);
        dataInput = new DataByteBufferArrayInputStream(inputStream);
    }

    public DataByteBufferArrayOutputStream getDataOutputStream(){
        return dataOutput;
    }

    public DataByteBufferArrayInputStream getDataInputStream() {
        return dataInput;
    }

    public String getAddressString(){
        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        if (address == null){
            return "unlocal";
        }
        return address.getHostString() + "|" + address.getPort();
    }

    public void sendHeart() throws IOException {
        dataOutput.writeInt(9999);
        dataOutput.flush();
    }

    public void prepare() throws IOException {
        dataOutput.writeInt(200);
    }

    public void writeAndFlushUtf(String msg) throws IOException {
        writeUTtf(msg);
        dataOutput.flush();
    }

    public void writeUTtf(String msg) throws IOException {
        dataOutput.writeUnrestrictedUtf(msg);
    }

    public String readUTF() throws IOException {
        return dataInput.readUnrestrictedUtf();
    }

    public void close(){
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public void ping(){
        lastPingTime = System.currentTimeMillis();
    }

    public boolean isClosed(){
        return socket.isClosed();
    }

    public long getLastPingTime() {
        return lastPingTime;
    }
}
