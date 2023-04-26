package com.black.socket.pool;

import com.black.http.SocketUtils;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.throwable.IOSException;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class SocketWrapper extends Socket{

    private final Socket socket;

    private final JHexByteArrayInputStream inputStream;

    private final JHexByteArrayOutputStream outputStream;

    public SocketWrapper(){
        socket = null;
        inputStream = null;
        outputStream = null;
    }

    public SocketWrapper(Socket socket) {
        this.socket = socket;
        try {
            inputStream = new JHexByteArrayInputStream(socket.getInputStream());
            outputStream = new JHexByteArrayOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public Socket getSocket() {
        return socket;
    }

    @Override
    public JHexByteArrayInputStream getInputStream(){
        return inputStream;
    }

    @Override
    public JHexByteArrayOutputStream getOutputStream(){
        return outputStream;
    }

    public JHexByteArrayInputStream writeAndWaitRead(Object source){
        if (socket != null){
            writeAndFlush(source);
            return waitRead();
        }
        return JHexByteArrayInputStream.EMPTY_STREAM;
    }

    public JHexByteArrayInputStream waitRead(){
        if (socket != null){
            try {
                return SocketUtils.read0(getInputStream(), socket);
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }else {
            return JHexByteArrayInputStream.EMPTY_STREAM;
        }
    }

    public void write(Object source){
        JHexByteArrayOutputStream outputStream = getOutputStream();
        if (outputStream != null){
            byte[] bytes = IoUtils.getBytes(source);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }
    }

    public void flush(){
        JHexByteArrayOutputStream outputStream = getOutputStream();
        if (outputStream != null){
            try {
                outputStream.flush();
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }
    }

    public void writeAndFlush(Object source){
        write(source);
        flush();
    }

    @Override
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    @Override
    public int getPort() {
        return socket.getPort();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return socket.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return socket.getChannel();
    }


    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        socket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return socket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        socket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return socket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        socket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        socket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return socket.getOOBInline();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        socket.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return socket.getSendBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        socket.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return socket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        socket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return socket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        socket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return socket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        socket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return socket.getReuseAddress();
    }

    @Override
    public synchronized void close() throws IOException {
        socket.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        socket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        socket.shutdownOutput();
    }

    @Override
    public String toString() {
        return socket.toString();
    }

    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    @Override
    public boolean isBound() {
        return socket.isBound();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return socket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return socket.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }
}
