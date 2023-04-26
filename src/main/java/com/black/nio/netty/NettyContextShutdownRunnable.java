package com.black.nio.netty;


import java.util.HashSet;
import java.util.Set;

public class NettyContextShutdownRunnable {

    private static final Set<NettyContext> contexts = new HashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (NettyContext context : contexts) {
                if (!context.isClosed()) {
                    context.close();
                }
            }
        }, "netty-shudown"));
    }

    public static void registerShutDownContext(NettyContext context){
        if (context != null){
            contexts.add(context);
        }
    }

    public static Set<NettyContext> getContexts() {
        return contexts;
    }


}
