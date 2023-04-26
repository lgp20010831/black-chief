package com.black.utils;

public abstract class AbstractLocal<L> implements Local<L>{

    protected final ThreadLocal<L> local = new ThreadLocal<>();

    @Override
    public L current() {
        L l = local.get();
        if (l == null){
            l = create();
            local.set(l);
        }
        return l;
    }

    abstract L create();

    @Override
    public void removeCurrent() {
        local.remove();
    }
}
