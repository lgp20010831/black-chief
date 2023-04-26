package com.black.core.builder;

import com.alibaba.fastjson.JSONArray;

import java.util.Collection;

public class JsonArrayBuilder {

    public static JsonArrayGovern builder(){
        return new JsonArrayGovern();
    }

    public static JsonArrayGovern machining(JSONArray array){
        return new JsonArrayGovern(array);
    }

    public static class JsonArrayGovern{

        final JSONArray array;

        public JsonArrayGovern(){
            this(new JSONArray());
        }

        public JsonArrayGovern(JSONArray array) {
            this.array = array;
        }

        public JsonArrayGovern add(Object value){
            array.add(value);
            return this;
        }

        public JsonArrayGovern addAll(Collection<? extends Object> list){
            array.addAll(list);
            return this;
        }

        public JSONArray build(){
            return array;
        }
    }

}
