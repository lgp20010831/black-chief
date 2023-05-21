package com.black.graphql.handler;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;
import com.black.graphql.GraphqlHandler;
import com.black.graphql.GraphqlObject;
import com.black.graphql.Utils;
import com.black.graphql.annotation.HttpHeaders;
import com.black.graphql.core.GraphqlClient;

import java.util.HashMap;
import java.util.Map;

public class HeaderHandler implements GraphqlHandler {

    @Override
    public boolean supportPrepare(MethodWrapper mw) {
        return mw.hasAnnotation(HttpHeaders.class) ||
                mw.getDeclaringClassWrapper().hasAnnotation(HttpHeaders.class);
    }


    @Override
    public void doPrepare(MethodWrapper mw, GraphqlObject object) {
        Map<String, String> map = new HashMap<>();
        HttpHeaders mwAnnotation = mw.getAnnotation(HttpHeaders.class);
        if (mwAnnotation != null){
            Map<String, String> header = Utils.parseHeader(mwAnnotation.headers());
            map.putAll(header);
        }

        HttpHeaders annotation = mw.getDeclaringClassWrapper().getAnnotation(HttpHeaders.class);
        if (annotation != null){
            Map<String, String> header = Utils.parseHeader(annotation.headers());
            map.putAll(header);
        }
        object.getClient().setHttpHeaders(map);
    }

    @Override
    public boolean supportParam(ParameterWrapper pw, MethodWrapper mw) {
        return pw.hasAnnotation(HttpHeaders.class);
    }

    @Override
    public void doParseParam(ParameterWrapper pw, Object val, GraphqlObject object) {
        if (val != null){
            HttpHeaders annotation = pw.getAnnotation(HttpHeaders.class);
            String name = annotation == null ? pw.getName() : (StringUtils.hasText(annotation.value()) ? annotation.value() : pw.getName());
            GraphqlClient client = object.getClient();
            ClassWrapper<? extends GraphqlClient> clientWrapper = ClassWrapper.get(client.getClass());
            Map<String, String> httpHeaders = (Map<String, String>) clientWrapper.getField("httpHeaders").getValue(client);
            httpHeaders.put(name, val.toString());
        }
    }
}
