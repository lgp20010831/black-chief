package com.black.core.query;

/** support capacity expansion */
public class TendsArrayStack<E> extends ArrayStack<E> {

    private static final int DEFAULT_CAPACITY = 16;

    public TendsArrayStack(){
        this(DEFAULT_CAPACITY);
    }

    public TendsArrayStack(int len) {
        super(len);
    }

    @Override
    protected void capacityExpansion() {
        Object[] newData = new Object[data.length * 2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
}
