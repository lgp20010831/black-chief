package com.black.http;

import com.black.http.service.HttpConfiguration;
import com.black.http.service.HttpService;
import com.black.nio.group.*;

import java.util.concurrent.LinkedBlockingQueue;

public class HttpDispatcher {

    private final Configuration configuration;

    private final NioType type;

    private AimType aimType = AimType.POOL_SOCKET;

    private final LinkedBlockingQueue<HttpTransitExecutor> executors = new LinkedBlockingQueue<>();

    public HttpDispatcher(Configuration configuration, NioType type) {
        this.configuration = configuration;
        this.type = type;
        if (type == NioType.JHEX){
            throw new IllegalStateException("nio type is not support jhex");
        }
        executors.add(new SocketTransitExecutor());
        executors.add(new SocketPoolTransitExecutor());
    }

    public void setAimType(AimType aimType) {
        this.aimType = aimType;
    }

    public AimType getAimType() {
        return aimType;
    }

    public NioType getType() {
        return type;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Session open(){
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setBindPort(configuration.getHttpPort());
        httpConfiguration.setBindHost(configuration.getHttpHost());
        httpConfiguration.setHttpRequestHandler(() -> new HttpRpcRequestHandler(this));
        httpConfiguration.setNioThreadNum(configuration.getThreadNum());
        httpConfiguration.setType(type);
        HttpService httpService = new HttpService(httpConfiguration);
        return httpService.startHttpServer();
    }

    public LinkedBlockingQueue<HttpTransitExecutor> getExecutors() {
        return executors;
    }
}
