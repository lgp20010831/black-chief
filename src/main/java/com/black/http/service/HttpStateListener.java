package com.black.http.service;

import com.black.http.Request;
import com.black.core.asyn.AsynConfiguration;
import com.black.core.asyn.AsynConfigurationManager;
import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.log.IoLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HttpStateListener {

    private final HttpConfiguration configuration;

    private final Map<String, Request> requestRegister = new ConcurrentHashMap<>();

    public HttpStateListener(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    public void registerRequest(Request request){
        requestRegister.put(request.getHttpId(), request);
    }

    public void finishRequest(String id){
        requestRegister.remove(id);
    }

    public void start(){
        AsynConfiguration configuration = AsynConfigurationManager.getConfiguration();
        if (configuration.getTimeCorePoolSize() == 1) {
            configuration.setTimeCorePoolSize(5);
        }
        AsynGlobalExecutor.scheduleWithFixedDelay(new LogRequestProgress(), 0, 10, TimeUnit.SECONDS);
    }


    class LogRequestProgress implements Runnable{

        @Override
        public void run() {
            IoLog log = configuration.getLog();
            int requestSize = requestRegister.size();
            if (configuration.isHttpDebug()) {
                log.debug("当前存在 {} 个正在处理的请求", requestSize);
            }
        }
    }
}
