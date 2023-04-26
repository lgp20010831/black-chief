package com.black.api;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;

import java.sql.Connection;
import java.util.Map;

import static com.black.api.ApiV2Utils.*;

public class ApiResponseBlentStrengthenResolver extends AbstractApiResponseSyntaxResolver{

    private final ApiRequestResolver apiRequestResolver;

    public ApiResponseBlentStrengthenResolver() {
        super("$S: ");
        apiRequestResolver = new ApiRequestResolver();
    }

    @Override
    public Object resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        Connection connection = (Connection) source.get(CONNECTION_NAME);
        AliasColumnConvertHandler handler = (AliasColumnConvertHandler) source.get(ALIAS_COLUMN_NAME);
        Class<?> controllerType  = (Class<?>) source.get(CONTROLLER_TYPE_NAME);
        MethodWrapper mw = (MethodWrapper) source.get(METHOD_WRAPPER_NAME);
        apiRequestResolver.setConnection(connection);
        Class<?> javaClass = apiRequestResolver.parseRequest(expression);
        return castClassToRequestJsonExplain(javaClass);
    }
}
