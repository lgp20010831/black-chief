package com.black.graphql;

import com.black.graphql.annotation.GraphqlClient;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.http.code.HttpUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GraphqlLayer implements AgentLayer {

    final GraphqlCrib crib;

    final Class<?> mapperClass;

    final String fixUrl;

    final ThreadLocal<Map<Method, GraphqlObject>> objectCache = new ThreadLocal<>();

    public GraphqlLayer(GraphqlCrib crib, Class<?> mapperClass) {
        this.crib = crib;
        this.mapperClass = mapperClass;
        GraphqlClient annotation = mapperClass.getAnnotation(GraphqlClient.class);
        if (annotation == null){
            throw new IllegalStateException("mapper class need assembling annotation @GraphqlClient");
        }
        fixUrl = HttpUtils.parseUrl(annotation.value(), "${", "}",
                ApplicationConfigurationReaderHolder.getReader().getMasterAndSubApplicationConfigSource());
    }

    @Override
    public Object proxy(AgentObject layer) {
        Method proxyMethod = layer.getProxyMethod();
        GraphqlObject graphqlObject = getObject().computeIfAbsent(proxyMethod, pm -> {
            return new GraphqlObject(fixUrl, crib);
        });
        try {
            return graphqlObject.doExecute(layer.getArgs(), MethodWrapper.get(proxyMethod));
        } catch (IOException e) {
            throw new GraphqlTransferException("call failed !!!", e);
        }
    }

    Map<Method, GraphqlObject> getObject(){
        Map<Method, GraphqlObject> map = objectCache.get();
        if (map == null){
            map = new HashMap<>();
            objectCache.set(map);
        }
        return map;
    }
}
