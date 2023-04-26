package com.black.nio.code.buf;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class PoolByteBufferGroup {

    int cap;

    private byte[] globalBuffer;

    private volatile boolean expansion;

    //已分配容量
    private final AtomicInteger assigned;

    //跟随全局 buffer 下标
    private final AtomicInteger globalIndex;

    private final Map<Integer, List<PoolByteBuffer>> zda = new ConcurrentHashMap<>();

    public PoolByteBufferGroup(){
        this(1024 * 1024);
    }

    public PoolByteBufferGroup(int cap) {
        this.cap = cap;
        globalIndex = new AtomicInteger(0);
        assigned = new AtomicInteger(0);
    }

    void initBuffer(){
        if (globalBuffer == null)
            globalBuffer = new byte[cap];
    }

    //分配 buffer
    public PoolByteBuffer allotment(int cap){
        initBuffer();
        log.info("尝试分配 buffer, 容量大小: {}", cap);
        //coordinate(cap);
        //获取针对该容量下已经创建出的 buffer 对象
        List<PoolByteBuffer> buffers = zda.computeIfAbsent(cap, ca -> new ArrayList<>());
        for (PoolByteBuffer buffer : buffers) {
            if (buffer.getRef() == 0){
                //如果 buffer 此时没有其他人引用, 则返回该 buffer
                buffer.increaseRef();
                assigned.addAndGet(cap);
                return buffer;
            }
        }

        //判断是否内存还够分配
        int length = globalBuffer.length;
        if (cap > length - globalIndex.get()){

            //如果不够分配了, 进行扩容
            capacityExpansion(cap);
            //throw new UnableAllocateBufferException("too much fragmentation");
            return allotment(cap);
        }

        //新分配对象, 创建buffer对象
        PoolByteBuffer buffer = new PoolByteBuffer(this
                ,globalBuffer, globalIndex.getAndSet(globalIndex.get() + cap), globalIndex.get());
        assigned.addAndGet(cap);
        //增加引用, 并保存起来
        buffer.increaseRef();
        buffers.add(buffer);
        return buffer;
    }

    //扩容, 扩容很消耗资源
    protected void capacityExpansion(int cap){
        expansion = true;
        try {
            int length = globalBuffer.length;
            //创建一个更大的缓冲区
            byte[] newBuf = new byte[length * 2 + cap];
            //将数据 copy 进去
            System.arraycopy(globalBuffer, 0, newBuf, 0, length);
            //替换所有管理的 buffer 中的 buf
            for (List<PoolByteBuffer> buffers : zda.values()) {
                for (PoolByteBuffer buffer : buffers) {
                    try {
                        Field buf = PoolByteBuffer.class.getDeclaredField("buf");
                        buf.setAccessible(true);
                        buf.set(buffer, newBuf);
                    } catch (NoSuchFieldException | IllegalAccessException e) {}
                }
            }
            globalBuffer = newBuf;
        }finally {
            //将扩容标记变成 false, 这样管理下的 buffer 可以进行写操作了
            expansion = false;
        }
    }

    //以旧换新
    public PoolByteBuffer tradeOldForNew(PoolByteBuffer buffer, int cap){
        //重新分配新的 buffer
        PoolByteBuffer newBuffer = allotment(buffer.limit() + cap);

        //记录读标记的位置
        int readIndex = buffer.getReadIndex();
        //将原 buffer 数据写到新 buffer 中
        for (byte b : buffer.toByteArray()) {
            newBuffer.writeByte(b);
        }
        //更新读标记位置, 然后释放调原 buffer
        newBuffer.readIndex = readIndex;
        buffer.release();
        return newBuffer;
    }

    public boolean isExpansion() {
        return expansion;
    }

    protected void coordinate(int cap){
        int length = globalBuffer.length;
        int i = length - assigned.get();
        if (cap > i){
            throw new UnableAllocateBufferException("无法在分配 buffer, 容量: " + length +
                    ", 可分配数" + i);
        }
    }

    void release(PoolByteBuffer buffer){
        int limit = buffer.limit();
        assigned.compareAndSet(assigned.get(), assigned.get() - limit);
    }
}
