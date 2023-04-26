package com.black.pool.bytes;

import com.black.pool.AbstractIdlePool;
import com.black.pool.Configuration;

public class ByteArrayPool extends AbstractIdlePool<Bytes> {

    private final int size;

    public ByteArrayPool(Configuration configuration, int size) {
        super(configuration);
        this.size = size;
    }

    @Override
    protected boolean structureInit() {
        return false;
    }

    @Override
    protected Bytes create0() {
        return new Bytes(size);
    }


}
