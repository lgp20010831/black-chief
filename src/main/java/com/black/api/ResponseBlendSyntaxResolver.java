package com.black.api;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;

import java.sql.Connection;
import java.util.Map;

import static com.black.api.ApiV2Utils.*;

public class ResponseBlendSyntaxResolver extends AbstractApiResponseSyntaxResolver{

    public ResponseBlendSyntaxResolver() {
        super("");
    }

    @Override
    public Object resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        Object data = null;
        Connection connection = (Connection) source.get(CONNECTION_NAME);
        AliasColumnConvertHandler handler = (AliasColumnConvertHandler) source.get(ALIAS_COLUMN_NAME);
        Class<?> controllerType  = (Class<?>) source.get(CONTROLLER_TYPE_NAME);
        MethodWrapper mw = (MethodWrapper) source.get(METHOD_WRAPPER_NAME);
        JDBCBlent blent = parseBlends(expression);
        if (blent != null){
            if (blent.json){
                data = parseBlent(blent, connection, handler, false, controllerType);
            }else {
                data = parseBlentArray(blent, connection, handler, false, controllerType);
            }
        }
        return data;
    }
}
