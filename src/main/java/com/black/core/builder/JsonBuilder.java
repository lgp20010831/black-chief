package com.black.core.builder;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class JsonBuilder {

    public static JsonGovern builder(){
        return new JsonGovern();
    }

    public static JsonGovern machining(JSONObject jsonObject){
        return new JsonGovern(jsonObject);
    }


    public static class JsonGovern{

        final JSONObject json;

        public JsonGovern(){
            this(new JSONObject());
        }

        public JsonGovern(JSONObject json) {
            this.json = json;
        }

        public JsonGovern put(String key, Object value){
            json.put(key, value);
            return this;
        }

        public JsonGovern putAll(Map<String, Object> map){
            json.putAll(map);
            return this;
        }

        public JSONObject build(){
            return json;
        }
    }

}
