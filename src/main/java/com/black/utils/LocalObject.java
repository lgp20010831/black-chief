package com.black.utils;

import lombok.NonNull;

import java.util.function.Supplier;

public class LocalObject<O> implements Local<O>{

    private final ThreadLocal<O> local = new ThreadLocal<>();

    private final Supplier<O> supplier;

    public LocalObject(@NonNull Supplier<O> supplier){
        this.supplier = supplier;
    }

    public void set(O o){
        local.set(o);
    }

    @Override
    public O current() {
        O o = local.get();
        if (o == null){
            o = supplier.get();
            local.set(o);
        }
        return o;
    }

    @Override
    public void removeCurrent() {
        local.remove();
    }
}
