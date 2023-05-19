package com.black.token;

import com.alibaba.fastjson.JSONObject;
import com.black.sql_v2.period.AttributeHandler;

/**
 * @author 李桂鹏
 * @create 2023-05-19 18:11
 */
@SuppressWarnings("all")
public class MapAttributeHandler implements AttributeHandler {

    private final JSONObject json;

    public MapAttributeHandler(JSONObject json) {
        this.json = json == null ? new JSONObject() : json;
    }

    @Override
    public JSONObject getFormData() {
        return json;
    }
}
