package com.black.thread;

import lombok.NonNull;

@SuppressWarnings("all")
public class WaitUtils {


    public static void wait0(@NonNull Object lock){
        wait0(lock, null, null);
    }

    public static void wait0(@NonNull Object lock, Runnable before, Runnable after){
        synchronized (lock){
            if (before != null){
                before.run();
            }
            try {
                lock.wait();
            } catch (InterruptedException e) {

            }
            if (after != null){
                after.run();
            }
        }
    }

    public static void notifyAll0(@NonNull Object lock){
        notifyAll0(lock, null, null);
    }

    public static void notifyAll0(@NonNull Object lock, Runnable before, Runnable after){
        synchronized (lock){
            if (before != null){
                before.run();
            }
            lock.notifyAll();
            if (after != null){
                after.run();
            }
        }
    }

    public static void notify0(@NonNull Object lock){
        notify0(lock, null, null);
    }

    public static void notify0(@NonNull Object lock, Runnable before, Runnable after){
        synchronized (lock){
            if (before != null){
                before.run();
            }
            lock.notify();
            if (after != null){
                after.run();
            }
        }
    }
}
