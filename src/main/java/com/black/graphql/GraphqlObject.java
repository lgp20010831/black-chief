package com.black.graphql;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.http.code.HttpUtils;
import com.black.core.query.MethodWrapper;
import com.black.graphql.annotation.Mutation;
import com.black.graphql.annotation.Query;
import com.black.graphql.core.GraphqlClient;
import com.black.graphql.core.request.GraphqlRequest;
import com.black.graphql.core.request.mutation.DefaultGraphqlMutation;
import com.black.graphql.core.request.mutation.GraphqlMutation;
import com.black.graphql.core.request.query.DefaultGraphqlQuery;
import com.black.graphql.core.request.query.GraphqlQuery;
import com.black.graphql.core.response.GraphqlResponse;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Log4j2
public class GraphqlObject {

    final GraphqlClient client;

    GraphqlRequest request;

    final GraphqlCrib crib;

    Map<Parameter, GraphqlHandler> paramCache = new ConcurrentHashMap<>();

    Map<Method, GraphqlHandler> resultCache = new ConcurrentHashMap<>();

    public GraphqlObject(String url, GraphqlCrib crib) {
        this.crib = crib;
        url = HttpUtils.parseUrl(url, "${", "}",
                ApplicationConfigurationReaderHolder.getReader().getMasterAndSubApplicationConfigSource());
        client = GraphqlClient.buildGraphqlClient(url);
    }

    public Object doExecute(Object[] args, MethodWrapper mw) throws IOException {
        createRequest(mw);

        for (GraphqlHandler handler : crib.getHandlers()) {
            if (handler.supportPrepare(mw)) {
                handler.doPrepare(mw, this);
            }
        }

        for (ParameterWrapper pw : mw.getParameterWrappersSet()) {
            GraphqlHandler graphqlHandler = paramCache.computeIfAbsent(pw.getParameter(), p -> {
                for (GraphqlHandler handler : crib.getHandlers()) {
                    if (handler.supportParam(pw, mw)) {
                        return handler;
                    }
                }
                return null;
            });

            if (graphqlHandler != null){
                graphqlHandler.doParseParam(pw, args[pw.getIndex()], this);
            }
        }

        GraphqlResponse response = doExecute0();
        Object result = response;
        GraphqlHandler graphqlHandler = resultCache.computeIfAbsent(mw.getMethod(), p -> {
            for (GraphqlHandler handler : crib.getHandlers()) {
                if (handler.supportResultType(p.getReturnType(), mw)) {
                    return handler;
                }
            }
            return null;
        });

        if (graphqlHandler != null){
            result = graphqlHandler.resolverResult(response, mw);
        }
        return result;
    }



    public void createRequest(MethodWrapper mw){
        if (mw.hasAnnotation(Query.class)) {
            request = new DefaultGraphqlQuery(mw.getAnnotation(Query.class).value());
        }else if (mw.hasAnnotation(Mutation.class)){
            request = new DefaultGraphqlMutation(mw.getAnnotation(Mutation.class).value());
        }else if (Utils.isQuery(mw.getName())){
            request = new DefaultGraphqlQuery(mw.getName());
        }else {
            request = new DefaultGraphqlMutation(mw.getName());
        }
    }

    public GraphqlResponse doExecute0() throws IOException {
        log.info("send graphql request: \n{}", request);
        if (request instanceof GraphqlMutation){
            return client.doMutation((GraphqlMutation) request);
        }else if (request instanceof GraphqlQuery){
            return client.doQuery((GraphqlQuery) request);
        }else {
            throw new IllegalStateException("exception request object: " + request);
        }
    }

}
