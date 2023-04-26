package com.black.http.service;

import com.black.http.Request;
import com.black.http.Response;
import com.black.core.log.IoLog;
import com.black.core.util.Assert;

import java.util.function.Supplier;

public class LastRequestFilter extends BaseFilter{

    private final HttpConfiguration configuration;

    public LastRequestFilter(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void doFilterRequest(FilterChain chain, Request request) throws Throwable {
        IoLog log = configuration.getLog();
        Supplier<HttpRequestHandler> handlerSupplier = configuration.getHttpRequestHandler();
        HttpRequestHandler httpRequestHandler = handlerSupplier.get();
        Assert.notNull(httpRequestHandler, "not find request handler");
        if (configuration.isHttpDebug()) {
            log.debug("执行 http 请求处理器: {}", httpRequestHandler);
        }

        Response response = httpRequestHandler.handle(request);
        Assert.notNull(response, "null response");
        HttpHolder.responseLocal.set(response);
        BaseFilterChain baseFilterChain = (BaseFilterChain) chain;
        baseFilterChain.setResponse(response);
        super.doFilterRequest(chain, request);
    }
}
