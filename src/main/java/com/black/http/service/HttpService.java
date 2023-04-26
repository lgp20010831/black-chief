package com.black.http.service;

import com.black.nio.group.Configuration;
import com.black.nio.group.Factorys;
import com.black.nio.group.Session;
import com.black.nio.group.SessionFactory;

public class HttpService {

    private final HttpConfiguration configuration;

    public HttpService(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    public HttpConfiguration getConfiguration() {
        return configuration;
    }

    public Session startHttpServer(){
        SessionFactory factory = Factorys.open(configuration.getBindHost(), configuration.getBindPort(),
                        new HttpGioResolver(configuration), configuration.getType())
                .apply(configuration.getGioConfigurationConsumer());
        Configuration configuration = factory.getConfiguration();
        configuration.setIoThreadNum(this.configuration.getNioThreadNum());
        this.configuration.flush();
        this.configuration.getStateListener().start();
        return factory.openSession();
    }
}
