package com.black.nio.code.run;

import java.util.concurrent.Callable;

public class FutureCallable<V> implements Futures<V>{

    final CallableFuture<V> future;

    final Callable<V> callable;

    public FutureCallable(CallableFuture<V> future, Callable<V> callable) {
        this.future = future;
        this.callable = callable;
    }


    @Override
    public void run() {
        try {
            if (!future.isCancel()) {
                try {
                    V call = callable.call();
                    future.result = call;
                } catch (Exception e) {
                    future.error = true;
                    future.throwable = e;
                    throw new RuntimeException(e);
                }
            }
            future.done = true;
        }finally {
            for (FutureListener listener : future.getListeners()) {
                listener.callback(future);
            }
        }
    }

    @Override
    public Future<V> getFuture() {
        return future;
    }
}
