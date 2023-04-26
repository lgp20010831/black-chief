package com.black.nio.code.run;

public class FutureRunnable implements Futures<Object>{

    final AbstractFuture<?> future;

    final Runnable runnable;

    public FutureRunnable(AbstractFuture<?> future, Runnable runnable) {
        this.future = future;
        this.runnable = runnable;
    }

    @Override
    public void run(){
        try {
            if (!future.isCancel()) {
                try {
                    runnable.run();
                }catch (RuntimeException e){
                    future.error = true;
                    future.throwable = e;
                    return;
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
    public Future<Object> getFuture() {
        return (Future<Object>) future;
    }
}
