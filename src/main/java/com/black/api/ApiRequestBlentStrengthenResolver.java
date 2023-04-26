package com.black.api;

import com.alibaba.fastjson.JSONObject;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;

import java.sql.Connection;
import java.util.Map;

import static com.black.api.ApiV2Utils.*;
import static com.black.api.ApiV2Utils.METHOD_WRAPPER_NAME;

@SuppressWarnings("ALL")
public class ApiRequestBlentStrengthenResolver extends AbstractApiRequestSyntaxResolver{

    private final ApiRequestResolver apiRequestResolver;

    public ApiRequestBlentStrengthenResolver() {
        super("$S: ");
        apiRequestResolver = new ApiRequestResolver();
    }

    @Override
    public void resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        Connection connection = (Connection) source.get(CONNECTION_NAME);
        AliasColumnConvertHandler handler = (AliasColumnConvertHandler) source.get(ALIAS_COLUMN_NAME);
        Class<?> controllerType  = (Class<?>) source.get(CONTROLLER_TYPE_NAME);
        MethodWrapper mw = (MethodWrapper) source.get(METHOD_WRAPPER_NAME);
        apiRequestResolver.setConnection(connection);
        Class<?> javaClass = apiRequestResolver.parseRequest(expression);
        JSONObject explain = castClassToRequestJsonExplain(javaClass);
        String explainStr = JSONTool.formatJson(explain.toString());
        httpMethod.setRequestJSON(explain);
        httpMethod.setRequestDome(explainStr);
        JSONObject demo = castClassToRequestJsonDemo(javaClass);
        String demoStr = JSONTool.formatJson(demo.toString());
        httpMethod.setRequestInvokeDome(demoStr);
    }
}
