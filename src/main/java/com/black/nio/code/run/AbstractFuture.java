package com.black.nio.code.run;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractFuture<C> implements Future<C>{

    protected final List<FutureListener> listeners = new ArrayList<>();

    volatile boolean done = false;

    volatile boolean error = false;

    Throwable throwable;

    volatile AtomicBoolean cancel = new AtomicBoolean(false);

    public boolean isCancel(){
        return cancel.get();
    }

    @Override
    public boolean isError() {
        return error;
    }

    public void cancel(){
        cancel.set(true);
    }

    public boolean isDone(){
        return !isCancel() && done;
    }

    public abstract C get();

    public abstract C get(long timeout);

    public List<FutureListener> getListeners() {
        return listeners;
    }

    public void addListener(FutureListener listener){
        if (listener != null)
            listeners.add(listener);
    }
}
