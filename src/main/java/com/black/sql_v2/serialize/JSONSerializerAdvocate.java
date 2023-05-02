package com.black.sql_v2.serialize;

import com.alibaba.fastjson.JSONObject;
import com.black.utils.ServiceUtils;

import java.util.Map;

public class JSONSerializerAdvocate extends AbstractSerializeAdvocate{

    @Override
    public boolean support(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public String toSerialize(Object value) {
        Map<String, Object> map = (Map<String, Object>) value;
        return new JSONObject(map).toJSONString();
    }

    @Override
    public Object deSerialize(String text, Class<?> type) {
        Map<Object, Object> map = ServiceUtils.createMap(type);
        map.putAll(JSONObject.parseObject(text));
        return map;
    }
}
