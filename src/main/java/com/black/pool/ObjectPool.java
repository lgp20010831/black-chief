package com.black.pool;

import com.black.function.Supplier;

@SuppressWarnings("all")
public class ObjectPool<T> extends AbstractIdlePool<T>{

    private final Supplier<T> supplier;

    private String closeMethodName;

    public ObjectPool(Configuration configuration, Supplier<T> supplier){
        this(configuration, supplier, null);
    }

    public ObjectPool(Configuration configuration, Supplier<T> supplier, String closeMethodName) {
        super(configuration);
        this.supplier = supplier;
        this.closeMethodName = closeMethodName;
    }

    @Override
    public T getElement() {
        return super.getElement();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public String getCloseMethodName() {
        return closeMethodName;
    }

    @Override
    protected boolean structureInit() {
        return false;
    }

    @Override
    protected T create0() throws Throwable {
        return supplier.get();
    }
}
