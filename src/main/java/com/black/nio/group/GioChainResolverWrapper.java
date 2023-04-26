package com.black.nio.group;

import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;

public class GioChainResolverWrapper implements GioChainResolver{

    private final GioResolver resolver;

    private GioChainResolverWrapper next;

    public GioChainResolverWrapper(){
        resolver = null;
    }

    public GioChainResolverWrapper(GioResolver resolver) {
        this.resolver = resolver;
    }

    public void setNext(GioChainResolverWrapper next) {
        this.next = next;
    }

    public GioChainResolverWrapper getNext() {
        return next;
    }

    @Override
    public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
        resolver.read(context, bytes, out);
    }

    @Override
    public void connectCompleted(GioContext context) {
        resolver.connectCompleted(context);
    }

    @Override
    public void acceptCompleted(GioContext context, JHexByteArrayOutputStream out) {
        resolver.acceptCompleted(context, out);
    }

    @Override
    public void write(GioContext context, Object source) {
        resolver.write(context, source);
    }

    @Override
    public void trowable(GioContext context, Throwable ex, JHexByteArrayOutputStream out) {
        resolver.trowable(context, ex, out);
    }

    @Override
    public void close(GioContext context) {
        resolver.close(context);
    }
}
