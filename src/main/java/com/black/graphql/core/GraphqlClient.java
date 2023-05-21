package com.black.graphql.core;

import com.alibaba.fastjson.JSONObject;
import com.black.graphql.core.request.GraphqlRequestType;
import com.black.graphql.core.request.mutation.GraphqlMutation;
import com.black.graphql.core.request.query.GraphqlQuery;
import com.black.graphql.core.response.DefaultGraphqlResponse;
import com.black.graphql.core.response.GraphqlResponse;
import com.black.graphql.core.util.HttpClientUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class GraphqlClient {

    private HttpClientUtil httpClientUtil = new HttpClientUtil();
    private String graphqlServerUrl = null;
    private Map<String, String> httpHeaders = new HashMap();

    private GraphqlClient(String graphqlUrl) {
        this.graphqlServerUrl = graphqlUrl;
    }

    public static GraphqlClient buildGraphqlClient(String graphqlUrl) {
        GraphqlClient graphqlClient = new GraphqlClient(graphqlUrl);
        return graphqlClient;
    }

    public <T extends GraphqlQuery> GraphqlResponse doQuery(T query) throws IOException {
        return this.doQuery(query, GraphqlRequestType.POST);
    }

    public <T extends GraphqlQuery> GraphqlResponse doQuery(T query, GraphqlRequestType graphqlRequestType) throws IOException {
        String json = query.toString();
        String result = this.doHttpRequest(json, graphqlRequestType);
        if (result == null) {
            return null;
        } else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            DefaultGraphqlResponse response = new DefaultGraphqlResponse();
            response.putAll(jsonObject);
            return response;
        }
    }

    public <T extends GraphqlMutation> GraphqlResponse doMutation(T mutation) throws IOException {
        return this.doMutation(mutation, GraphqlRequestType.POST);
    }

    public <T extends GraphqlMutation> GraphqlResponse doMutation(T mutation, GraphqlRequestType graphqlRequestType) throws IOException {
        String json = mutation.toString();
        String result = this.doHttpRequest(json, graphqlRequestType);
        if (result == null) {
            return null;
        } else {
            JSONObject jsonObject = JSONObject.parseObject(result);
            DefaultGraphqlResponse response = new DefaultGraphqlResponse();
            response.putAll(jsonObject);
            return response;
        }
    }

    private String doHttpRequest(String json, GraphqlRequestType type) throws IOException {
        String result = null;
        if (type.equals(GraphqlRequestType.POST)) {
            result = this.httpClientUtil.doPostJson(this.graphqlServerUrl, json, this.httpHeaders);
        }

        return result;
    }

    public void setHttpHeaders(Map<String, String> headers) {
        this.httpHeaders = headers;
    }

}
