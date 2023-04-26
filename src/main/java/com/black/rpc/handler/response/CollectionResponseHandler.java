package com.black.rpc.handler.response;

import com.alibaba.fastjson.JSONArray;
import com.black.rpc.response.Response;
import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;

import java.util.*;

public class CollectionResponseHandler extends AbstractResponseHandler{
    @Override
    public boolean support(MethodWrapper mw, Response response) {
        return Collection.class.isAssignableFrom(mw.getReturnType());
    }

    @Override
    protected Object resolveUtfResponseBody(MethodWrapper mw, String utfBody) {
        JSONArray array = JSONArray.parseArray(utfBody);
        Class<?> returnType = mw.getReturnType();
        Collection<Object> collection;
        if (BeanUtil.isSolidClass(returnType)){
            collection = (Collection<Object>) ReflexUtils.instance(returnType);
        }else if (Set.class.isAssignableFrom(returnType)){
            collection = new HashSet<>();
        }else if (List.class.isAssignableFrom(returnType)){
            collection = new ArrayList<>();
        }else {
            throw new IllegalStateException("unknown collection type of: " + returnType);
        }
        collection.addAll(array);
        return collection;
    }
}
