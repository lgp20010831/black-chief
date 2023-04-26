package com.black.test;

import com.black.core.log.Catalog;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Setter  @Getter
public class Configuration {

    ApiInfoGetter apiInfoGetter;

    List<ApiInvoker> invokers = new ArrayList<>();

    String complateUrl;

    String ip;

    int port;

    Catalog log;

    boolean concurrency = false;

    int parallelValue = 3;

    final Connection connection;

    boolean annotationLimit = true;

    public Configuration(Connection connection) {
        this.connection = connection;
        apiInfoGetter = new DefaultApiInfoGetter(connection);
        log = new ApiLog();
        invokers.add(new DefaultGetApiInvoker(log));
        invokers.add(new DefaultPostApiInvoker(log));
    }

    public String getComplateUrl() {
        if(complateUrl == null){
            if (ip == null){
                throw new IllegalArgumentException("需要指定完整 url");
            }
            complateUrl = "http://" + ip + ":" + port;
        }
        return complateUrl;
    }
}
