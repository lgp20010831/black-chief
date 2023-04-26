package com.black.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.AnnotationUtils;

import java.sql.Connection;
import java.util.Map;

import static com.black.api.ApiV2Utils.*;

@SuppressWarnings("all")
public class BlendJdbcSyntaxResolver extends AbstractApiRequestSyntaxResolver {


    private final MultPartRequestResolver multPartRequestResolver;

    public BlendJdbcSyntaxResolver() {
        super("");
        multPartRequestResolver = new MultPartRequestResolver();
    }

    @Override
    public void resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        Connection connection = (Connection) source.get(CONNECTION_NAME);
        AliasColumnConvertHandler handler = (AliasColumnConvertHandler) source.get(ALIAS_COLUMN_NAME);
        Class<?> controllerType  = (Class<?>) source.get(CONTROLLER_TYPE_NAME);
        MethodWrapper mw = (MethodWrapper) source.get(METHOD_WRAPPER_NAME);
        //如果是个 multiPart 请求
        if (ApiV2Utils.isMuiltPartRequest(httpMethod, mw)){
            multPartRequestResolver.resolverMultPartRequest(httpMethod, mw);
            return;
        }
        JDBCBlent blent = parseBlends(expression);
        if (blent == null)
            return;
        boolean brj = remarkJoin;
        remarkJoin = false;
        try {
            JSON json;
            if (blent.json){
                JSONObject object = parseBlent(blent, connection, handler, true, controllerType);
                if (mw != null){
                    Map<String, Object> map = AnnotationUtils.getAnnotation(mw.getMethod(), PageTools.class);
                    if (map != null){
                        object.put(map.get("pageSize").toString(), 1);
                        object.put(map.get("pageNum").toString(), 1);
                    }
                }
                json = object;
            }else {
                json = parseBlentArray(blent, connection, handler, true, controllerType);
            }
            String formatJson = JSONTool.formatJson(json.toString());
            httpMethod.setRequestDome(formatJson);
            httpMethod.setRequestJSON(json);
            httpMethod.setRequestInvokeDome(formatJson);
        }finally {
            remarkJoin = brj;
        }


    }


}
