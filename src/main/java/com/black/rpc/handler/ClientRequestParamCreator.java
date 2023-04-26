package com.black.rpc.handler;

import com.black.rpc.request.Request;
import com.black.rpc.RpcConfiguration;
import com.black.rpc.annotation.WriteBody;
import com.black.rpc.inter.RequestDistinctBuilder;
import com.black.rpc.inter.RequestParamResolver;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
public class ClientRequestParamCreator {

    private final DefaultRequestParamResolver defaultRequestParamResolver;

    private final RequestDistinctBuilder defaultRequestBuilder;

    private final RpcConfiguration configuration;

    public ClientRequestParamCreator(RpcConfiguration configuration) {
        this.configuration = configuration;
        defaultRequestParamResolver = new DefaultRequestParamResolver();
        defaultRequestBuilder = new DefaultRequestBuildler();
    }

    public Request createRequest(MethodWrapper mw, Object[] args) throws IOException {
        RequestParamCarrier paramCarrier = new RequestParamCarrier();

        resolveWriteBody(mw, args, paramCarrier);
        if (!paramCarrier.isWriteBody()) {
            paramCarrier.initRequestJson();
            resolveParams(mw, args, paramCarrier);
        }

        Object requestParam = paramCarrier.getRequestParam();
        Request request = doCreateRequest(mw, args, requestParam);
        log.info("create request is [{}]", request);
        return request;
    }

    private Request doCreateRequest(MethodWrapper mw, Object[] args, Object requestParam) throws IOException {
        LinkedBlockingQueue<RequestDistinctBuilder> requestDistinctBuilders = configuration.getRequestDistinctBuilders();
        RequestDistinctBuilder builder = null;
        for (RequestDistinctBuilder distinctBuilder : requestDistinctBuilders) {
            if (distinctBuilder.support(mw)) {
                builder = distinctBuilder;
                break;
            }
        }
        if (builder == null){
            builder = defaultRequestBuilder;
        }
        return builder.createRequest(mw, requestParam, args);
    }

    private void resolveParams(MethodWrapper mw, Object[] args, RequestParamCarrier paramCarrier){
        LinkedBlockingQueue<RequestParamResolver> requestParamResolvers = configuration.getRequestParamResolvers();
        for (ParameterWrapper pw : mw.getParameterWrappersSet()) {
            RequestParamResolver resolver = null;
            for (RequestParamResolver paramResolver : requestParamResolvers) {
                if (paramResolver.support(mw, pw)) {
                    resolver = paramResolver;
                    break;

                }
            }
            if (resolver == null){
                resolver = defaultRequestParamResolver;
            }
            resolver.resolverParam(mw, pw, args[pw.getIndex()], paramCarrier);
        }
    }

    private void resolveWriteBody(MethodWrapper mw, Object[] args, RequestParamCarrier paramCarrier){
        if (mw.parameterHasAnnotation(WriteBody.class)) {
            ParameterWrapper pw = mw.getSingleParameterByAnnotation(WriteBody.class);
            paramCarrier.setWriteBody(args[pw.getIndex()]);
        }
    }
}
