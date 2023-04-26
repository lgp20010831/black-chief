package com.black.nio.code;

import java.util.Collection;
import java.util.HashSet;

public class NioShutdownThreadHook extends Thread{

    private static NioShutdownThreadHook hook;

    private boolean register = false;

    private final Collection<NioContext> contexts = new HashSet<>();

    public static NioShutdownThreadHook getInstance() {
        if (hook == null)
            hook = new NioShutdownThreadHook();
        return hook;
    }

    public synchronized void registerHook(){
        if (register) return;
        try {
            Runtime.getRuntime().addShutdownHook(this);
        }finally {
            register = true;
        }
    }

    public void addContext(NioContext context){
        if (context != null){
            contexts.add(context);
        }
    }

    @Override
    public void run() {
        for (NioContext context : contexts) {
            try {
                context.shutdownNow();
            }catch (Throwable e){}
        }
    }
}
