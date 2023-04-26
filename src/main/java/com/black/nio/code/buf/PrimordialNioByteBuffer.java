package com.black.nio.code.buf;

import com.black.nio.code.Configuration;
import com.black.nio.code.NioChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PrimordialNioByteBuffer implements NioByteBuffer{

    private PoolByteBuffer buffer;

    private final Configuration configuration;

    private static PoolByteBufferGroup staticByteBufferGroup;

    public static int globalBufferSize = 1024 * 1024;

    private static void initStaticBufferGroup(){
        if (staticByteBufferGroup == null){
            staticByteBufferGroup = new PoolByteBufferGroup(globalBufferSize);
        }
    }

    public PrimordialNioByteBuffer(Configuration configuration) {
        if (configuration.getContext() == null) {
            throw new IllegalStateException("current config not bind nio context");
        }
        this.buffer = configuration.getContext()
                .getBufferGroup()
                .allotment(configuration.getAcceptBufferSize());
        this.configuration = configuration;
    }


    public PrimordialNioByteBuffer(int size){
        configuration = null;
        initStaticBufferGroup();
        buffer = staticByteBufferGroup.allotment(size);
    }

    public PoolByteBuffer getByteBuffer() {
        return buffer;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void writeBytes(byte[] bytes) {
        if (bytes != null){
            PoolByteBuffer byteBuffer = getByteBuffer();
            int writable = byteBuffer.getWritable();
            if (writable >= bytes.length){
                //如果可以全部写进去
                byteBuffer.writeByte(bytes);
            }else {
                //如果不能全部写进去, 只能进行扩容
                PoolByteBuffer newBuffer = byteBuffer.getGroup().tradeOldForNew(byteBuffer, bytes.length * 2);
                buffer = newBuffer;
                writeBytes(bytes);
            }
        }
    }

    @Override
    public void write(NioChannel channel) throws IOException {
        SocketChannel socketChannel = (SocketChannel) channel.channel();
        //分配一块缓冲区
        ByteBuffer byteBuffer = buffer.getWriteByteBuffer();
        try {
            //写数据到操作系统缓冲区
            socketChannel.write(byteBuffer);

            //每一此真正将数据刷到 channel 后, 清空缓冲区
            buffer.clear();
        }finally {
            //回收 buffer
            ByteBufferManager.recovery(byteBuffer);
        }
    }

    @Override
    public byte[] read(NioChannel channel) throws IOException, SocketReadCloseException {
        SocketChannel socketChannel = (SocketChannel) channel.channel();
        ByteBuffer byteBuffer = ByteBufferManager.allocate(buffer.limit());
        try {
            //将操作系统缓冲区数据读到 buffer 里
            int size = socketChannel.read(byteBuffer);
            if (size == -1){
                throw new SocketReadCloseException();
            }
            return read0(byteBuffer, size);
        }finally {
            ByteBufferManager.recovery(byteBuffer);
        }
    }

    protected byte[] read0(ByteBuffer byteBuffer, int size){
        int readIndex = buffer.getReadIndex();
        if (size <= buffer.getWritable()){
            //可读
            //将 buffer 里的数据读到 poolBuffer 中
            buffer.writeByte(byteBuffer);
        }else {
            //不够读
            if (size <= buffer.limit()){
                buffer.clear();
            }else {

                //要给 buffer 进行扩容
                //释放调当前 buffer, 重新分配一块 buffer
                buffer.release();
                buffer = buffer.getGroup().allotment(size + 50);
            }
            return read0(byteBuffer, size);
        }
        ////return buffer.toByteArray(readIndex, buffer.getReadable());
        return buffer.readByte(new byte[size]);
    }

    public void setBuffer(PoolByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void clear() {
        buffer.clear();
    }

    @Override
    public byte[] toByteArray() {
        return buffer.toByteArray();
    }

    @Override
    public void release() {
        buffer.release();
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
