package com.black.core.sql.code.shutdown;


import com.black.core.sql.code.SQLApplicationContext;

import java.util.Collection;
import java.util.HashSet;

public class SQLContextShutdownHook extends Thread{

    private static SQLContextShutdownHook hook;

    private boolean register = false;

    private final Collection<SQLApplicationContext> contexts = new HashSet<>();

    public static SQLContextShutdownHook getInstance() {
        if (hook == null)
            hook = new SQLContextShutdownHook();
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

    public void addContext(SQLApplicationContext context){
        if (context != null){
            contexts.add(context);
        }
    }

    @Override
    public void run() {
        for (SQLApplicationContext context : contexts) {
            try {
                context.shutdown();
            }catch (Throwable e){}
        }
    }
}
