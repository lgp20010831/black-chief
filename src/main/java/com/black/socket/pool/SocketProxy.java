package com.black.socket.pool;

import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.log.IoLog;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;

@SuppressWarnings("all")
public class SocketProxy implements ApplyProxyLayer {

    private final SocketPool pool;

    private Socket proxy0;

    private Socket origin;

    private boolean core;

    //占用标记
    private boolean employ = false;

    public SocketProxy(SocketPool pool) {
        this.pool = pool;
    }

    public boolean isEmploy() {
        return employ;
    }

    public void setOrigin(Socket origin) {
        this.origin = origin;
    }

    public Socket getOrigin() {
        return origin;
    }

    public void setProxy0(Socket proxy0) {
        this.proxy0 = proxy0;
    }

    public void setEmploy(boolean employ) {
        this.employ = employ;
    }

    public Socket getProxy0() {
        return proxy0;
    }

    public void setCore(boolean core) {
        this.core = core;
    }

    public SocketPool getPool() {
        return pool;
    }

    public boolean isCore() {
        return core;
    }

    public void closeSocket(){
        IoLog log = pool.getLog();
        log.info("close socket: {}", proxy0);
        try {
            origin.close();
        } catch (IOException e) {

        }
    }

    @Override
    public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
        String name = method.getName();
        if (name.equals("shutdownInput") || name.equals("shutdownOutput") || name.equals("close")){
            pool.releaseConnection(this);
            return null;
        }
        return template.invokeOriginal(args);
    }

    @Override
    public String toString() {
        return "[proxy] ==> " + origin;
    }
}
