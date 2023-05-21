package com.black.core.util;

import com.alibaba.fastjson.JSONObject;
import com.black.sql_v2.period.AttributeHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("all")
public class Body extends JSONObject implements AttributeHandler {

    public Body() {
        super(true);
    }

    public Body(Map<String, Object> map) {
        super(map == null ? new LinkedHashMap<>() : map);
    }

    public Body put(String key, Object value){
        super.put(key, value);
        return this;
    }

    public Body putAll0(Map<? extends String, ?> map){
        if (map != null){
            super.putAll(map);
        }
        return this;
    }

    public Body puts(String k1, Object v1, String k2, Object v2){
        return jf(new String[]{k1, k2}, new Object[]{v1, v2});
    }
    public Body puts(String k1, Object v1, String k2, Object v2, String k3, Object v3){
        return jf(new String[]{k1, k2, k3}, new Object[]{v1, v2, v3});
    }
    public Body puts(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4){
        return jf(new String[]{k1, k2, k3, k4}, new Object[]{v1, v2, v3, v4});
    }
    public Body puts(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5){
        return jf(new String[]{k1, k2, k3, k4, k5}, new Object[]{v1, v2, v3, v4, v5});
    }
    public Body puts(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6){
        return jf(new String[]{k1, k2, k3, k4, k5, k6}, new Object[]{v1, v2, v3, v4, v5, v6});
    }
    public Body puts(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7){
        return jf(new String[]{k1, k2, k3, k4, k5, k6, k7}, new Object[]{v1, v2, v3, v4, v5, v6, v7});
    }

    public Body jf(String[] ks, Object[] vs){
        int size = ks.length;
        if (size != vs.length){
            throw new RuntimeException("ks.size != vs.size");
        }
        for (int i = 0; i < ks.length; i++) {
            put(ks[i], vs[i]);
        }
        return this;
    }

    @Override
    public JSONObject getFormData() {
        return this;
    }
}
