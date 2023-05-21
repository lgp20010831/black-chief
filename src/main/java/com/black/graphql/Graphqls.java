package com.black.graphql;

import com.alibaba.fastjson.JSONObject;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.graphql.core.request.GraphqlRequest;
import com.black.graphql.core.request.mutation.DefaultGraphqlMutation;
import com.black.graphql.core.request.query.DefaultGraphqlQuery;
import com.black.graphql.core.request.result.ResultAttributtes;
import com.black.graphql.core.util.HttpClientUtil;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("all") @Log4j2
public class Graphqls {


    /*
        query getLoginInfo{
        getLoginInfo(deviceType: #{type}"1"){
        messages{
            message
        }
        successful
        result{
            actionList{
                id
                pId
                name
                weight
                icon
                path
                type
                state
                perms
                clientType
            }
            departmentKeyList
            departmentNameList
            employeeKey
            employeeName
            user
            roleCodeList
            roleWeight
          }
        }
    }
     */

    public static String agreePrefix = "graphql.url";

    private static String parseUrl(){
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        return reader.selectAttribute(agreePrefix);
    }

    public static GraphqlFetchBuilder query(String requestName){
        return query(parseUrl(), requestName);
    }

    public static GraphqlFetchBuilder query(String url, String requestName){
        return new GraphqlFetchBuilder(url, requestName).type(true);
    }

    public static GraphqlFetchBuilder mutation(String requestName){
        return mutation(parseUrl(), requestName);
    }

    public static GraphqlFetchBuilder mutation(String url, String requestName){
        return new GraphqlFetchBuilder(url, requestName).type(false);
    }

    public static class GraphqlFetchBuilder {
        private final String url;

        private String requestJson;

        private String requestName;

        private GraphqlRequest request;

        private boolean query = true;

        private Map<String, String> httpHeaders = new LinkedHashMap<>();

        private final HttpClientUtil clientUtil = new HttpClientUtil();

        public GraphqlFetchBuilder(String url){
            this.url = url;
        }

        public GraphqlFetchBuilder(String url, String requestName) {
            this.url = url;
            this.requestName = requestName;
        }

        public GraphqlFetchBuilder type(boolean query){
            this.query = query;
            return this;
        }

        public GraphqlFetchBuilder requestName(String requestName){
            this.requestName = requestName;
            if (request != null){
                request.setRequestName(requestName);
            }
            return this;
        }

        public GraphqlFetchBuilder addParam(String key, Object val){
            if (request == null){
                request = query ? new DefaultGraphqlQuery(requestName) : new DefaultGraphqlMutation(requestName);
            }
            request.addParameter(key, val);
            return this;
        }

        public GraphqlFetchBuilder addObjectParam(String key, Object val){
            if (request == null){
                request = query ? new DefaultGraphqlQuery(requestName) : new DefaultGraphqlMutation(requestName);
            }
            request.getRequestParameter().addObjectParameter(key, val);
            return this;
        }

        public GraphqlFetchBuilder addParams(Map<String, Object> params){
            if (request == null){
                request = query ? new DefaultGraphqlQuery(requestName) : new DefaultGraphqlMutation(requestName);
            }
            params.forEach((k, v) -> request.addParameter(k, v));
            return this;
        }

        public GraphqlFetchBuilder addResults(String txt){
            if (request == null){
                request = query ? new DefaultGraphqlQuery(requestName) : new DefaultGraphqlMutation(requestName);
            }
            ResultAttributtes[] resultAttributtes = Utils.parseResultAttributes(txt);
            request.addResultAttributes(resultAttributtes);
            return this;
        }

        public GraphqlFetchBuilder addResults(ResultAttributtes... resultAttr){
            if (request == null){
                request = query ? new DefaultGraphqlQuery(requestName) : new DefaultGraphqlMutation(requestName);
            }
            request.addResultAttributes(resultAttr);
            return this;
        }

        public GraphqlFetchBuilder addHeader(String k, String v){
            httpHeaders.put(k, v);
            return this;
        }

        public GraphqlFetchBuilder addHeaders(Map<String, String> map){
            httpHeaders.putAll(map);
            return this;
        }

        public GraphqlFetchBuilder request(String txt, Map<String, Object> paramMap){
            String requestTxt = ServiceUtils.parseTxt(txt, "#{", "}", item -> {
                Object value = paramMap.get(item);
                if (value == null) {
                    return "null";
                }
                return "\"" + value.toString() + "\"";
            });
            this.requestJson = requestTxt;
            return this;
        }

        public JSONObject fetch(){
            try {
                if (requestJson == null){
                    if (request == null){
                        throw new GraphqlTransferException("no request to fetch");
                    }
                    requestJson = request.toString();
                }
                log.info("fetch graphql request: \n{}", requestJson);
                String response = clientUtil.doPostJson(url, requestJson, httpHeaders);
                if (response == null) {
                    return null;
                } else {
                    return JSONObject.parseObject(response);
                }
            } catch (IOException e) {
                throw new GraphqlTransferException(e);
            }
        }

        public String getRequestMessage(){
            if (requestJson == null){
                if (request == null){
                    throw new GraphqlTransferException("no request to fetch");
                }
                requestJson = request.toString();
            }
            return requestJson;
        }
    }


}
