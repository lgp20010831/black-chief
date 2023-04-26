package com.black.socket;

import java.util.concurrent.locks.LockSupport;

public class Parkable {

    protected Thread runnableThread;

    protected volatile boolean park = false;

    protected void park(){
        runnableThread = Thread.currentThread();
        park = true;
        LockSupport.park();
    }

    protected void unpark(){
        if (park){
            park = false;
            if (runnableThread != null){
                LockSupport.unpark(runnableThread);
            }
        }
    }
}
