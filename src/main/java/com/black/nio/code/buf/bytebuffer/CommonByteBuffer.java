package com.black.nio.code.buf.bytebuffer;

import com.black.nio.code.Configuration;
import com.black.nio.code.NioChannel;
import com.black.nio.code.buf.NioByteBuffer;
import com.black.nio.code.buf.SocketReadCloseException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

public class CommonByteBuffer implements NioByteBuffer {

    private final Configuration configuration;

    private ByteBuffer buffer;

    public CommonByteBuffer(Configuration configuration) {
        this.configuration = configuration;
    }

    void initBuffer(){
        if (buffer == null)
            buffer = ByteBuffer.allocate(configuration.getAcceptBufferSize());
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public synchronized void writeBytes(byte[] bytes) {
        if (bytes != null){
            initBuffer();
            int l = bytes.length;
            isWritableElseExpansion(l);
            buffer.put(bytes);
        }
    }

    @Override
    public synchronized void write(NioChannel channel) throws IOException {
        initBuffer();
        SelectableChannel selectableChannel = channel.channel();
        SocketChannel socketChannel = (SocketChannel) selectableChannel;
        buffer.flip();
        socketChannel.write(buffer);
    }

    void isWritableElseExpansion(int l){
        int p = buffer.position();
        int c = buffer.capacity();
        if (l > c - p){
            buffer = expansion(buffer, c + l);
        }
    }

    ByteBuffer expansion(ByteBuffer oldBuffer, int len){
        ByteBuffer newByteBuffer = ByteBuffer.allocate(len);
        //转成可读
        oldBuffer.flip();
        final int l = oldBuffer.limit();
        for (int i = 0; i < l; i++) {
            newByteBuffer.put(oldBuffer.get());
        }
        return newByteBuffer;
    }

    @Override
    public byte[] read(NioChannel channel) throws IOException, SocketReadCloseException {
        initBuffer();
        SocketChannel socketChannel = (SocketChannel) channel.channel();
        try {
            for (;;){
                //还有多少字节可写
                int writable = buffer.capacity() - buffer.position();
                int size = socketChannel.read(buffer);
                if (size == -1){
                    throw new SocketReadCloseException();
                }

                if (size != writable){
                    break;
                }
                buffer = expansion(buffer, buffer.capacity() * 2);
            }
            return toByteArray();
        }finally {
            clear();
        }
    }

    @Override
    public byte[] toByteArray() {
        initBuffer();
        buffer.flip();
        int limit = buffer.limit();
        byte[] bytes = new byte[limit];
        buffer.get(bytes, 0, limit);
        return bytes;
    }

    @Override
    public void release() {
        clear();
        buffer = null;
    }

    @Override
    public void clear() {
        initBuffer();
        buffer.clear();
    }

    @Override
    public String toString() {
        initBuffer();
        return "CommonByteBuffer[" +
                "cap =" + buffer.capacity() +
                ", limit =" + buffer.limit() +
                ", position =" + buffer.position() +
                ']';
    }
}
