package com.black.nio.code.buf;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ByteBufferManager {

    private final static Map<Integer, LinkedBlockingQueue<ByteBuffer>> bufferPool = new ConcurrentHashMap<>();

    public static ByteBuffer wrap(byte[] bytes){
        return ByteBuffer.wrap(bytes);
    }

    public static ByteBuffer allocate(int cap){
        LinkedBlockingQueue<ByteBuffer> bufferQueue = bufferPool.computeIfAbsent(cap, cp -> new LinkedBlockingQueue<>());
        if (bufferQueue.isEmpty()){
            return ByteBuffer.allocate(cap);
        }
        return bufferQueue.poll();
    }

    public static void recovery(ByteBuffer buffer){
        if (buffer == null){
            return;
        }
        int capacity = buffer.capacity();
        buffer.clear();
        LinkedBlockingQueue<ByteBuffer> bufferQueue = bufferPool.computeIfAbsent(capacity, cap -> new LinkedBlockingQueue<>());
        try {
            bufferQueue.put(buffer);
        } catch (InterruptedException e) {
            //ignore
        }
    }
}
