package com.black.share;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.*;
import com.black.utils.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shkstart
 * @create 2023-05-04 10:16
 */
public abstract class ShareServer implements GioResolver, MethodInvoker {

    protected final String host;

    protected final int port;

    protected Session session;

    protected final Map<String, GioContext> contextMap = new ConcurrentHashMap<>();

    protected ShareServer(String host, int port) {
        this.host = host;
        this.port = port;
        enabledServer();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void reset(Object process){}

    public void shutdown(){
        if (!contextMap.isEmpty()){
            GioContext gioContext = CollectionUtils.firstValue(contextMap);
            cultivateMainBranch(gioContext);
        }
        session.shutdown();
    }

    public Map<String, GioContext> getContextMap() {
        return contextMap;
    }

    protected void cultivateMainBranch(GioContext context){

    }

    @Override
    public Object invokeMethod(String name, Object... args) {
        return null;
    }

    protected NioType chooseNioType(){
        return NioType.NETTY;
    }

    protected void enabledServer(){
        session = Factorys.open(this, chooseNioType())
                .apply(configuration -> {
                    configuration.setHost(host);
                    configuration.setPort(port);
                })
                .openSession();
    }

    @Override
    public void acceptCompleted(GioContext context, JHexByteArrayOutputStream out) {
        String address = context.bindAddress();
        contextMap.put(address, context);
        GioResolver.super.acceptCompleted(context, out);
    }

    @Override
    public void close(GioContext context) {
        contextMap.remove(context.bindAddress());
        GioResolver.super.close(context);
    }

    @Override
    public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
        List<Request> requests = ShareUtils.resolveRequest(bytes);
        for (Request request : requests) {
            resolveRequest(request, context);
        }
    }

    protected void resolveRequest(Request request, GioContext context){
        Response response;
        try {
            Object result = handleRequest(request);
            response = new NormalResponse(result);
        } catch (Throwable e) {
            response = new ThrowableResponse(e);
        }
        response.setResponseId(request.getRequestId());
        byte[] bytes = ShareUtils.createResponseBytes(response);
        context.writeAndFlush(bytes);
    }

    protected abstract Object handleRequest(Request request) throws Throwable;
}
