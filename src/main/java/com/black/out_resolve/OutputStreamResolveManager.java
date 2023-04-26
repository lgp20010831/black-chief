package com.black.out_resolve;

import com.black.pattern.InstanceQueue;
import com.black.resolve.ResolveException;

import java.io.OutputStream;
import java.util.Collection;


@SuppressWarnings("all")
public class OutputStreamResolveManager {

    private OutputStreamResolveManager(){}

    private static InstanceQueue<OutputStreamResolver> resolverInstanceQueue;

    static {
        resolverInstanceQueue = InstanceQueue.scan("com.example.out_resolve.impl", true);
    }

    public static void register(Class<? extends OutputStreamResolver> type){
        resolverInstanceQueue.registerType(type);
    }

    public static void resolve(OutputStream outputStream, Object rack, Object value) {
        Collection<OutputStreamResolver> resolvers = resolverInstanceQueue.getInstances();
        for (OutputStreamResolver resolver : resolvers) {
            if (resolver.support(rack)) {
                try {
                    resolver.doResolve(outputStream, rack, value);
                } catch (Throwable throwable) {
                    throw new ResolveException(throwable);
                }
                return;
            }
        }
    }

}
