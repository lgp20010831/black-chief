package com.black.http.service;

import com.black.http.HttpRequestUtils;
import com.black.http.HttpResponseUtils;
import com.black.http.Request;
import com.black.http.Response;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.core.log.IoLog;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.utils.IdUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Supplier;

public class HttpGioResolver implements GioResolver {

    private final HttpConfiguration configuration;

    private final IoLog log;

    public HttpGioResolver(HttpConfiguration configuration) {
        this.configuration = configuration;
        log = configuration.getLog();
    }

    @Override
    public void acceptCompleted(GioContext context, JHexByteArrayOutputStream out) {
        if (configuration.isHttpDebug()) {
            log.debug("接到客户端连接: {}", context.remoteAddress());
        }
        GioResolver.super.acceptCompleted(context, out);
    }

    @Override
    public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
        if (configuration.isHttpDebug()){
            log.debug("接收到客户端: {} 请求报文: {}", context.remoteAddress(), new String(bytes));
        }

        HttpStateListener stateListener = configuration.getStateListener();
        Request request = parseRequest(bytes);
        //创建一个唯一 http id
        String short8Id = IdUtils.createShort8Id();
        request.setHttpId(short8Id);
        HttpHolder.requestLocal.set(request);
        try {
            stateListener.registerRequest(request);
            Response response = fireRequest(request);
            String responseString = HttpResponseUtils.toResponseString(response);
            if (configuration.isHttpDebug()) {
                log.debug("响应客户端:{} 报文: {}", context.remoteAddress(), responseString);
            }
            context.writeAndFlush(responseString);
        }finally {
            HttpHolder.requestLocal.remove();
            HttpHolder.responseLocal.remove();
            stateListener.finishRequest(short8Id);
        }
    }

    private Response fireRequest(Request request){
        HttpThrowableHandler throwableHandler = configuration.getThrowableHandler();
        ThrowableResponseBuilder throwableResponseBuilder = configuration.getThrowableResponseBuilder();
        BaseFilterChain chain = new BaseFilterChain();
        chain.setRequest(request);
        LinkedList<Supplier<Filter>> filters = configuration.getFilters();
        for (Supplier<Filter> supplier : filters) {
            chain.addFilter(supplier.get());
        }
        try {

            chain.fireRequest();
        } catch (Throwable e) {
            if (configuration.isHttpDebug()){
                CentralizedExceptionHandling.handlerException(e);
            }
            Object result = null;
            //请求中发生异常
            if (throwableHandler != null){
                result = throwableHandler.resolveThrowable(e, request);
            }
            return throwableResponseBuilder.build(e, result);
        }

        try {
            chain.fireResponse();
        } catch (Throwable e) {
            //处理响应时发生异常, 忽略
        }
        return HttpHolder.responseLocal.get();
    }

    private Request parseRequest(byte[] bytes) throws IOException {
        return HttpRequestUtils.parseRequest(bytes);
    }


    @Override
    public void write(GioContext context, Object source) {
        if (configuration.isHttpDebug()) {
            log.debug("写向客户端: {}", context.remoteAddress());
        }
        GioResolver.super.write(context, source);
    }

    @Override
    public void close(GioContext context) {
        if (configuration.isHttpDebug()) {
            log.debug("感知到客户端断开连接: {}", context.remoteAddress());
        }
        GioResolver.super.close(context);
    }


}
