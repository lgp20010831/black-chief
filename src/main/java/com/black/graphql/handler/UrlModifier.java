package com.black.graphql.handler;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.graphql.GraphqlHandler;
import com.black.graphql.GraphqlObject;
import com.black.graphql.annotation.UrlAddress;
import com.black.graphql.core.GraphqlClient;
import com.black.graphql.core.response.GraphqlResponse;



public class UrlModifier implements GraphqlHandler {

    @Override
    public boolean supportResultType(Class<?> returnType, MethodWrapper mw) {
        return GraphqlResponse.class.isAssignableFrom(returnType);
    }

    @Override
    public Object resolverResult(GraphqlResponse response, MethodWrapper mw) {
        return response;
    }

    @Override
    public boolean supportPrepare(MethodWrapper mw) {
        return mw.hasAnnotation(UrlAddress.class);
    }

    @Override
    public void doPrepare(MethodWrapper mw, GraphqlObject object) {
        UrlAddress address = mw.getAnnotation(UrlAddress.class);
        String url = address.value();
        ClassWrapper<? extends GraphqlClient> clientWrapper = ClassWrapper.get(object.getClient().getClass());
        clientWrapper.getField("graphqlServerUrl").setValue(object.getClient(), url);
    }

    @Override
    public boolean supportParam(ParameterWrapper pw, MethodWrapper mw) {
        return pw.hasAnnotation(UrlAddress.class);
    }

    @Override
    public void doParseParam(ParameterWrapper pw, Object val, GraphqlObject object) {
        ClassWrapper<? extends GraphqlClient> clientWrapper = ClassWrapper.get(object.getClient().getClass());
        clientWrapper.getField("graphqlServerUrl").setValue(object.getClient(), val == null ? "null" : val.toString());
    }
}
