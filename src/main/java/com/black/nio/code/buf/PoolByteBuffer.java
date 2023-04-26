package com.black.nio.code.buf;


import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

//简单适配 ByteBuffer
public class PoolByteBuffer {

    final AtomicInteger ref = new AtomicInteger(0);

    private final PoolByteBufferGroup group;

    private byte[] buf;

    int readIndex;

    int writeIndex;

    private final int start;

    private final int end;

    public PoolByteBuffer(PoolByteBufferGroup group, byte[] buf, int start, int end){
        this(group, buf, start, end, 0, 0);
    }

    public PoolByteBuffer(PoolByteBufferGroup group, byte[] buf,
                          int start, int end, int readIndex,
                          int writeIndex) {
        this.buf = buf;
        this.start = start;
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.end = end;
        this.group = group;
        if (start > buf.length || end > buf.length){
            throw new IllegalArgumentException("the start and end positions should be valid");
        }
    }

    public int limit(){
        return end - start;
    }

    public PoolByteBufferGroup getGroup() {
        return group;
    }

    protected int getWritePosition(){
        return start == 0 ? writeIndex++ : start + writeIndex++;
    }

    protected int getReadPosition(){
        return start == 0 ? readIndex++ : start + readIndex++;
    }

    public void writeByte(byte b){
        canRef();
        allowWrite();
        if (writeIndex >= limit()){
            throw new BufferWriteLimitException("limit:" + (end - start));
        }
        buf[getWritePosition()] = b;
    }

    public void writeByte(ByteBuffer buffer){
        if(buffer == null)return;
            buffer.flip();
            int limit = buffer.limit();
            int writeLen = Math.min(limit, getWritable());
            for (int i = 0; i < writeLen; i++) {
                writeByte(buffer.get());
            }
    }

    public void readByte(ByteBuffer buffer){
        int readable = getReadable();
        for (int i = 0; i < readable; i++) {
            buffer.put((byte) readByte());
        }
    }

    //向外读
    public int readByte(){
        canRef();
        if (readIndex >= end){
            throw new BufferUnreadableException("read already size:" + getReadIndex());
        }
        return buf[getReadPosition()];
    }

    //读到数组中
    public byte[] readByte(byte[] tarBuf){
        canRef();
        checkBuf(tarBuf);
        int readSize = Math.min(getReadable(), tarBuf.length);
        for (int i = 0; i < readSize; i++) {
            tarBuf[i] = (byte) readByte();
        }
        return tarBuf;
    }

    //向里写
    public void writeByte(byte[] srcBuf){
        canRef();
        checkBuf(srcBuf);
        int writeSize = Math.min(getWritable(), srcBuf.length);
        for (int i = 0; i < writeSize; i++) {
            writeByte(srcBuf[i]);
        }
    }

    public int getReadIndex() {
        return readIndex;
    }

    public int getReadable(){
        return getWriteIndex() - getReadIndex();
    }

    public int getWritable(){
        return limit() - getWriteIndex();
    }

    public int getWriteIndex() {
        return writeIndex;
    }

    protected void checkBuf(byte[] buf){
        if (buf == null){
            throw new IllegalArgumentException("buf is null");
        }
    }

    public void allowWrite(){
        for (;;){
            //如果组在扩容, 则自旋等待
            if (!group.isExpansion()){
                break;
            }
        }
    }

    public int getRef() {
        return ref.get();
    }

    public int increaseRef(){
        return ref.incrementAndGet();
    }

    public ByteBuffer getWriteByteBuffer(){
        return ByteBufferManager.wrap(toByteArray());
    }

    public void canRef(){
        if (ref.get() == 0){
            throw new CannotReferenceBufferException("0");
        }
    }

    public byte[] toByteArray(int offset, int len){
        if (len > getWriteIndex()) {
            throw new IndexOutOfBoundsException("LIMIT: " + getWriteIndex());
        }
        byte[] buf0 = new byte[len];
        System.arraycopy(buf, offset >= start ? offset : start + offset, buf0, 0, len);
        return buf0;
    }

    public byte[] toByteArray(){
        return toByteArray(start, getWriteIndex());
    }

    public void clear(){
        BufferUtils.clearBuf(buf, start, end);
        readIndex = 0;
        writeIndex = 0;
    }

    public int release(){
        //减少引用
        if (ref.decrementAndGet() == 0) {
            clear();
            //并回收对象
            group.release(this);
        }
        return ref.get();
    }

    @Override
    public String toString() {
        return "PoolByteBuffer[" +
                "ref=" + ref +
                ", readIndex=" + readIndex +
                ", writeIndex=" + writeIndex +
                ", start=" + start +
                ", end=" + end +
                ']';
    }
}
