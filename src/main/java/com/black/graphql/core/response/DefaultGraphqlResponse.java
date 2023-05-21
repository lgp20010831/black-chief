package com.black.graphql.core.response;



import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultGraphqlResponse extends LinkedHashMap implements GraphqlResponse {
    public DefaultGraphqlResponse() {
    }

    public Map getData() {
        return this;
    }
}