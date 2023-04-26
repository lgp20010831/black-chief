package com.black.http.service;

import com.black.nio.group.Configuration;
import com.black.nio.group.NioType;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter @Setter
public class HttpConfiguration {

    private String bindHost = "0.0.0.0";

    private IoLog log;

    private int bindPort = 10000;

    private boolean httpDebug = true;

    private NioType type = NioType.CHIEF;

    private int nioThreadNum = 20;

    private ThrowableResponseBuilder throwableResponseBuilder;

    private HttpThrowableHandler throwableHandler;

    private final LinkedList<Supplier<Filter>> filters = new LinkedList<>();

    private Consumer<LinkedList<Supplier<Filter>>> filtersCallback;

    private Consumer<Configuration> gioConfigurationConsumer;

    private Supplier<HttpRequestHandler> httpRequestHandler;

    private HttpStateListener stateListener;

    public HttpConfiguration(){
        log = LogFactory.getLog4j();
        stateListener = new HttpStateListener(this);
        throwableResponseBuilder = new ThrowableResponseBuilder(this);
    }

    public void addFilter(Supplier<Filter> filter){
        filters.add(filter);
    }

    public void setType(NioType type) {
        if (type == NioType.JHEX) {
            throw new IllegalStateException("server must is not jhex");
        }
        this.type = type;
    }

    public void flush(){
        if (filtersCallback != null){
            filtersCallback.accept(filters);
        }
        filters.addLast(() -> new LastRequestFilter(this));
    }
}
