package com.black.graphql.core.request.query;


import com.black.graphql.core.request.GraphqlRequest;

public abstract class GraphqlQuery extends GraphqlRequest {
    protected GraphqlQuery(String requestName) {
        super(requestName);
    }

    public String toString() {
        String superStr = super.toString();
        return "{\"query\":\"{" + superStr + "}\"}";
    }
}
