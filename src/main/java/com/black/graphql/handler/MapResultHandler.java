package com.black.graphql.handler;

import com.alibaba.fastjson.JSONObject;
import com.black.graphql.GraphqlHandler;
import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import org.mountcloud.graphql.response.GraphqlResponse;

import java.util.Map;

public class MapResultHandler implements GraphqlHandler {

    @Override
    public boolean supportResultType(Class<?> returnType, MethodWrapper mw) {
        return Map.class.isAssignableFrom(returnType);
    }

    @Override
    public Object resolverResult(GraphqlResponse response, MethodWrapper mw) {
        Class<?> returnType = mw.getReturnType();
        if (returnType.equals(JSONObject.class)){
            return new JSONObject(response.getData());
        }
        if (BeanUtil.isSolidClass(returnType)){
            Map<String, Object> instance = (Map<String, Object>) ReflexUtils.instance(returnType);
            instance.putAll(response.getData());
            return instance;
        }
        return response.getData();
    }
}
