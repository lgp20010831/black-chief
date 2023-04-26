package com.black.nio.code.run;

public class CallableFuture<C> extends AbstractFuture<C>{

    C result;

    @Override
    public C get() {
        return get(-1L);
    }

    @Override
    public C get(long timeout) {
        long end = timeout == -1 ? -1 : System.currentTimeMillis() + timeout;
        for (;;){
            if (end == -1 || System.currentTimeMillis() >= end)
                break;

            if (isError()){
                if(throwable != null){
                    throw new FutureRunnableException("future runnable has error", throwable);
                }else {
                    throw new FutureRunnableException("future runnable has error");
                }
            }
        }
        return result;
    }
}
