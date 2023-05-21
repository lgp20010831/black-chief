package com.black.graphql.core.request.mutation;

import com.black.graphql.core.request.GraphqlRequest;

public abstract class GraphqlMutation extends GraphqlRequest {
    protected GraphqlMutation(String requestName) {
        super(requestName);
    }

    public String toString() {
        String superStr = super.toString();
        return "{\"query\":\"mutation{" + superStr + "}\"}";
    }
}