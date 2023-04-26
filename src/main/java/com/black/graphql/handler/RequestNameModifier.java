package com.black.graphql.handler;

import com.black.graphql.GraphqlHandler;
import com.black.graphql.GraphqlObject;
import com.black.graphql.annotation.RequestName;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;

public class RequestNameModifier implements GraphqlHandler {

    @Override
    public boolean supportPrepare(MethodWrapper mw) {
        return mw.hasAnnotation(RequestName.class);
    }

    @Override
    public void doPrepare(MethodWrapper mw, GraphqlObject object) {
        RequestName annotation = mw.getAnnotation(RequestName.class);
        String url = annotation.value();
        object.getRequest().setRequestName(url);
    }

    @Override
    public boolean supportParam(ParameterWrapper pw, MethodWrapper mw) {
        return pw.hasAnnotation(RequestName.class);
    }

    @Override
    public void doParseParam(ParameterWrapper pw, Object val, GraphqlObject object) {
        object.getRequest().setRequestName(val == null ? "null" : val.toString());
    }


}
