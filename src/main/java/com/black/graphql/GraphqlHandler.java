package com.black.graphql;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.graphql.core.response.GraphqlResponse;


public interface GraphqlHandler {

    default boolean supportPrepare(MethodWrapper mw){
        return false;
    }

    default void doPrepare(MethodWrapper mw, GraphqlObject object){

    }

    default boolean supportParam(ParameterWrapper pw, MethodWrapper mw){
        return false;
    }

    default void doParseParam(ParameterWrapper pw, Object val, GraphqlObject object){

    }

    default boolean supportResultType(Class<?> returnType, MethodWrapper mw){
        return false;
    }

    default Object resolverResult(GraphqlResponse response, MethodWrapper mw){
        return null;
    }
}
