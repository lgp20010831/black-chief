package com.black.sql_v2.utils;

import com.alibaba.fastjson.JSONObject;

public class JsonDrawer {

    private final String text;

    private final Object source;

    private JsonDrawer(String text, Object source) {
        this.text = text;
        this.source = source;
    }

    public static JsonDrawer draw(String text, Object source){
        return new JsonDrawer(text, source);
    }

    public JSONObject reach(){
        return SqlV2Utils.prepareJson(text, source);
    }

}
