package com.black.sql_v2.period;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author shkstart
 * @create 2023-04-23 11:12
 */
@SuppressWarnings("all")
public interface AttributeProcessor {

    JSONObject getFormData();


    default Object put(String key, Object value) {
        return getFormData().put(key, value);
    }


    default void putAll(Map<? extends String, ?> m) {
        getFormData().putAll(m);
    }
}
